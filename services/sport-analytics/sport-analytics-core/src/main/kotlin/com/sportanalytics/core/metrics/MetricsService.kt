package com.sportanalytics.core.metrics

import com.sportanalytics.core.infrastructure.ClickHouseClient
import com.sportanalytics.core.model.MatchMetrics
import com.sportanalytics.core.model.MatchStats
import com.sportanalytics.core.repository.MatchRepository
import com.sportanalytics.core.repository.MatchStatsRepository
import org.slf4j.LoggerFactory
import kotlin.math.exp

/**
 * Derived metrics calculation (diploma Лист 3, ГУИР.222403.003):
 * per-shot xG via logistic model xG_i = σ(β₀ + β₁·d + β₂·α + ...),
 * aggregated per team, then synced to ClickHouse match_metrics.
 *
 * Shot coordinates are not provided by the data source, so distance and
 * angle terms are approximated from shot quality (on-target share).
 */
class MetricsService(
    private val matchRepository: MatchRepository,
    private val matchStatsRepository: MatchStatsRepository,
    private val clickHouseClient: ClickHouseClient,
) {
    private val log = LoggerFactory.getLogger(MetricsService::class.java)

    companion object {
        // Logistic regression coefficients (model xg-v1)
        const val BETA_0 = -2.4
        const val BETA_1 = 1.6   // shot quality (on-target share)
        const val BETA_2 = 0.9   // attacking volume factor
        const val XA_SHARE = 0.72
        const val LEAGUE_AVG_XG = 1.35
    }

    fun calculateForLeague(leagueId: Int): Int {
        val pending = matchRepository.findPendingMetrics(leagueId)

        if (pending.isEmpty()) {
            return 0
        }

        clickHouseClient.ensureSchema()

        val (avgXg, avgXga) = clickHouseClient.avgMetrics()
            ?.takeIf { it.first > 0 && it.second > 0 }
            ?: (LEAGUE_AVG_XG to LEAGUE_AVG_XG) // bootstrap defaults (Лист 3)

        val rows = mutableListOf<MatchMetrics>()
        val processedIds = mutableListOf<Int>()

        for (match in pending) {
            val home = matchStatsRepository.findByMatchAndTeam(match.id, match.homeTeamId) ?: continue
            val away = matchStatsRepository.findByMatchAndTeam(match.id, match.awayTeamId) ?: continue

            val homeXg = expectedGoals(home)
            val awayXg = expectedGoals(away)

            rows += metricsRow(match.id, match.homeTeamId, match.matchDate, homeXg, awayXg, avgXg, avgXga)
            rows += metricsRow(match.id, match.awayTeamId, match.matchDate, awayXg, homeXg, avgXg, avgXga)
            processedIds += match.id
        }

        val inserted = clickHouseClient.insertMetrics(rows)

        if (inserted > 0) {
            matchRepository.markMetricsCalculated(processedIds)
        }

        log.info("Metrics calculated for {} matches ({} rows -> ClickHouse)", processedIds.size, inserted)

        return inserted
    }

    /** Aggregated xG: Σ over shots of σ(β₀ + β₁·quality + β₂·volume). */
    fun expectedGoals(stats: MatchStats): Double {
        val shots = stats.shots ?: 0
        val onTarget = stats.shotsOnTarget ?: 0

        if (shots == 0) return 0.0

        val quality = onTarget.toDouble() / shots
        val volume = (shots.toDouble() / 30.0).coerceAtMost(1.0)
        val perShot = sigmoid(BETA_0 + BETA_1 * quality + BETA_2 * volume)

        return round3(shots * perShot)
    }

    private fun metricsRow(
        matchId: Int,
        teamId: Int,
        matchDate: java.time.Instant,
        xg: Double,
        xga: Double,
        avgXg: Double,
        avgXga: Double,
    ) = MatchMetrics(
        matchId = matchId,
        teamId = teamId,
        matchDate = matchDate,
        xg = xg,
        xa = round3(xg * XA_SHARE),
        xga = xga,
        attackIndex = round3(xg / avgXg),
        defenceIndex = round3(avgXga / (xga + 0.1)),
    )

    private fun sigmoid(x: Double): Double = 1.0 / (1.0 + exp(-x))

    private fun round3(v: Double): Double = Math.round(v * 1000.0) / 1000.0
}
