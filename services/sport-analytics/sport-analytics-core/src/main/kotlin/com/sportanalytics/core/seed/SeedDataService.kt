package com.sportanalytics.core.seed

import com.sportanalytics.core.model.League
import com.sportanalytics.core.model.Match
import com.sportanalytics.core.model.Team
import com.sportanalytics.core.repository.LeagueRepository
import com.sportanalytics.core.repository.MatchRepository
import com.sportanalytics.core.repository.MatchStatsRepository
import com.sportanalytics.core.repository.TeamRepository
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class SeedDataService(
    private val leagueRepository: LeagueRepository,
    private val teamRepository: TeamRepository,
    private val matchRepository: MatchRepository,
    private val matchStatsRepository: MatchStatsRepository,
) {
    private val log = LoggerFactory.getLogger(SeedDataService::class.java)

    companion object {
        const val SEEDED_ROUNDS = 19
        const val ROUND_INTERVAL_DAYS = 7L
    }

    fun seed(): String {
        if (leagueRepository.findAll().isNotEmpty()) {
            return "Database already seeded"
        }

        log.info("Seeding Premier League {} data...", EplData.SEASON)

        val leagueId = leagueRepository.insert(
            League(name = EplData.LEAGUE_NAME, country = EplData.COUNTRY, season = EplData.SEASON),
        )

        val teamIds = EplData.TEAMS.map { t ->
            teamRepository.insert(Team(leagueId = leagueId, name = t.name, shortName = t.shortName))
        }

        val rng = Random(42)
        val simulator = MatchSimulator(rng)
        val now = Instant.now()
        var finishedCount = 0

        // Rounds 1..19 — finished matches with full statistics
        for (round in 1..SEEDED_ROUNDS) {
            val roundDate = now.minus((SEEDED_ROUNDS - round + 1) * ROUND_INTERVAL_DAYS, ChronoUnit.DAYS)

            for (fixture in FixtureGenerator.roundFixtures(teamIds.size, round)) {
                insertFinishedMatch(simulator, leagueId, teamIds, fixture, round, roundDate, rng)
                finishedCount++
            }
        }

        // Round 20 — upcoming scheduled matches (no results yet)
        val nextRound = SEEDED_ROUNDS + 1
        val scheduled = scheduleRound(leagueId, teamIds, nextRound, now.plus(3, ChronoUnit.DAYS), rng)

        val msg = "Seeded: 1 league, ${teamIds.size} teams, $finishedCount finished matches " +
            "($SEEDED_ROUNDS rounds), $scheduled scheduled (round $nextRound)"
        log.info(msg)

        return msg
    }

    fun insertFinishedMatch(
        simulator: MatchSimulator,
        leagueId: Int,
        teamIds: List<Int>,
        fixture: FixtureGenerator.Fixture,
        round: Int,
        roundDate: Instant,
        rng: Random,
    ): Int {
        val home = EplData.TEAMS[fixture.homeIdx]
        val away = EplData.TEAMS[fixture.awayIdx]
        val result = simulator.simulate(home, away)

        val matchId = matchRepository.insert(
            Match(
                leagueId = leagueId,
                homeTeamId = teamIds[fixture.homeIdx],
                awayTeamId = teamIds[fixture.awayIdx],
                matchDate = roundDate.plus(rng.nextLong(0, 48), ChronoUnit.HOURS),
                stadium = home.stadium,
                attendance = result.attendance,
                homeScore = result.homeGoals,
                awayScore = result.awayGoals,
                status = "finished",
                round = round,
                referee = result.referee,
                statsLoaded = true,
            ),
        )

        matchStatsRepository.insert(simulator.toMatchStats(matchId, teamIds[fixture.homeIdx], result.homeStats))
        matchStatsRepository.insert(simulator.toMatchStats(matchId, teamIds[fixture.awayIdx], result.awayStats))

        return matchId
    }

    fun scheduleRound(leagueId: Int, teamIds: List<Int>, round: Int, baseDate: Instant, rng: Random): Int {
        if (round > EplData.TOTAL_ROUNDS) {
            return 0
        }

        var count = 0
        for (fixture in FixtureGenerator.roundFixtures(teamIds.size, round)) {
            val home = EplData.TEAMS[fixture.homeIdx]
            matchRepository.insert(
                Match(
                    leagueId = leagueId,
                    homeTeamId = teamIds[fixture.homeIdx],
                    awayTeamId = teamIds[fixture.awayIdx],
                    matchDate = baseDate.plus(rng.nextLong(0, 48), ChronoUnit.HOURS),
                    stadium = home.stadium,
                    status = "scheduled",
                    round = round,
                ),
            )
            count++
        }

        return count
    }
}
