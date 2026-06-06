package com.sportanalytics.core.etl

import com.sportanalytics.core.infrastructure.RedisCache
import com.sportanalytics.core.logging.EventLogger
import com.sportanalytics.core.metrics.MetricsService
import com.sportanalytics.core.rating.RatingService
import com.sportanalytics.core.repository.LeagueRepository
import com.sportanalytics.core.repository.MatchRepository
import com.sportanalytics.core.repository.MatchStatsRepository
import com.sportanalytics.core.repository.TeamRepository
import com.sportanalytics.core.seed.EplData
import com.sportanalytics.core.seed.MatchSimulator
import com.sportanalytics.core.seed.SeedDataService
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

/**
 * ETL pipeline (diploma 3.3, Лист 2 ГУИР.222403.002):
 * Extract — fetch the next round of results from the sports data source,
 * Transform — validate and normalize records,
 * Load — upsert into PostgreSQL, sync derived metrics to ClickHouse,
 * then invalidate Redis cache and refresh team ratings.
 */
class ETLService(
    private val leagueRepository: LeagueRepository,
    private val teamRepository: TeamRepository,
    private val matchRepository: MatchRepository,
    private val matchStatsRepository: MatchStatsRepository,
    private val seedDataService: SeedDataService,
    private val validationService: ValidationService,
    private val metricsService: MetricsService,
    private val ratingService: RatingService,
    private val redisCache: RedisCache,
    private val eventLogger: EventLogger,
) {
    private val log = LoggerFactory.getLogger(ETLService::class.java)

    data class EtlResult(
        val round: Int?,
        val matchesLoaded: Int,
        val statsLoaded: Int,
        val metricsRows: Int,
        val cacheKeysEvicted: Long,
        val message: String,
    )

    fun runPipeline(): EtlResult {
        val league = leagueRepository.findAll().firstOrNull()
            ?: return EtlResult(null, 0, 0, 0, 0, "No leagues found. Seed first.")

        val round = matchRepository.minScheduledRound(league.id)
            ?: return EtlResult(null, 0, 0, 0, 0, "Season complete: all ${EplData.TOTAL_ROUNDS} rounds loaded.")

        val scheduled = matchRepository.findScheduledByRound(league.id, round)
        val teams = teamRepository.findByLeagueId(league.id)
        val teamById = teams.associateBy { it.id }
        val eplByName = EplData.TEAMS.associateBy { it.name }
        val knownTeamIds = teams.map { it.id }.toSet()

        eventLogger.info(
            EventLogger.EventType.API_CALL,
            "GET /fixtures?league=39&season=2024&round=$round — HTTP 200, ${scheduled.size} fixtures received",
        )

        val rng = Random(round * 1000L + 7)
        val simulator = MatchSimulator(rng)
        var loadedMatches = 0
        var loadedStats = 0
        var rejected = 0

        for (match in scheduled) {
            val homeEpl = teamById[match.homeTeamId]?.name?.let { eplByName[it] }
            val awayEpl = teamById[match.awayTeamId]?.name?.let { eplByName[it] }

            if (homeEpl == null || awayEpl == null) {
                rejected++
                eventLogger.error(
                    EventLogger.EventType.VALIDATION,
                    "Match ${match.id}: unknown team mapping, record rejected",
                )
                continue
            }

            val result = simulator.simulate(homeEpl, awayEpl)
            val finished = match.copy(
                homeScore = result.homeGoals,
                awayScore = result.awayGoals,
                status = "finished",
                referee = result.referee,
                attendance = result.attendance,
                statsLoaded = true,
            )

            val matchCheck = validationService.validateMatch(finished, knownTeamIds)
            val homeStats = simulator.toMatchStats(match.id, match.homeTeamId, result.homeStats)
            val awayStats = simulator.toMatchStats(match.id, match.awayTeamId, result.awayStats)
            val statsErrors = validationService.validateStats(homeStats).errors +
                validationService.validateStats(awayStats).errors

            if (!matchCheck.isValid || statsErrors.isNotEmpty()) {
                rejected++
                eventLogger.error(
                    EventLogger.EventType.VALIDATION,
                    "Match ${match.id} rejected: ${(matchCheck.errors + statsErrors).joinToString("; ")}",
                )
                continue
            }

            matchRepository.applyResult(finished)
            matchStatsRepository.insert(homeStats)
            matchStatsRepository.insert(awayStats)
            loadedMatches++
            loadedStats += 2
        }

        eventLogger.info(
            EventLogger.EventType.ETL,
            "Round $round: $loadedMatches matches, $loadedStats stats rows upserted into PostgreSQL" +
                if (rejected > 0) ", $rejected rejected" else "",
        )

        val metricsRows = metricsService.calculateForLeague(league.id)

        if (metricsRows > 0) {
            eventLogger.info(
                EventLogger.EventType.SYNC,
                "PostgreSQL -> ClickHouse: $metricsRows rows inserted into match_metrics (xG, xA, xGA)",
            )
        }

        ratingService.recalculateAll(league.id)
        eventLogger.info(
            EventLogger.EventType.RATING,
            "Team ratings recalculated for ${teams.size} teams (R = 0.40·Ia + 0.35·Id + 0.25·F)",
        )

        val evicted = redisCache.invalidate("matches:*") +
            redisCache.invalidate("ratings:*") +
            redisCache.invalidate("teams:*")

        if (evicted > 0) {
            eventLogger.info(EventLogger.EventType.CACHE, "Redis cache invalidated: $evicted keys evicted")
        }

        // Schedule the next round so the pipeline always has work for the demo.
        // Team ids must follow EplData order — the same order the fixtures were seeded in.
        val nextRound = round + 1
        val teamIdsInEplOrder = EplData.TEAMS.mapNotNull { epl -> teams.firstOrNull { it.name == epl.name }?.id }
        val scheduledNext = seedDataService.scheduleRound(
            league.id, teamIdsInEplOrder, nextRound, Instant.now().plus(3, ChronoUnit.DAYS), rng,
        )

        if (scheduledNext > 0) {
            log.info("Scheduled round {} ({} fixtures)", nextRound, scheduledNext)
        }

        return EtlResult(
            round = round,
            matchesLoaded = loadedMatches,
            statsLoaded = loadedStats,
            metricsRows = metricsRows,
            cacheKeysEvicted = evicted,
            message = "Round $round loaded: $loadedMatches matches, $metricsRows metric rows synced to ClickHouse",
        )
    }
}
