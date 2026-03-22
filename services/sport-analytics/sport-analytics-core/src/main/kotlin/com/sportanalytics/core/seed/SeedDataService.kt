package com.sportanalytics.core.seed

import com.sportanalytics.core.model.*
import com.sportanalytics.core.repository.*
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

/**
 * Populates the database with sample Premier League data
 * so predictions work with real match history.
 */
class SeedDataService(
    private val leagueRepository: LeagueRepository,
    private val teamRepository: TeamRepository,
    private val matchRepository: MatchRepository,
    private val matchStatsRepository: MatchStatsRepository,
) {
    private val log = LoggerFactory.getLogger(SeedDataService::class.java)

    fun seed(): String {
        if (leagueRepository.findAll().isNotEmpty()) {
            return "Database already seeded"
        }

        log.info("Seeding sample data...")

        // 1. League
        val leagueId = leagueRepository.insert(
            League(name = "Premier League", country = "England", season = "2025-2026"),
        )

        // 2. Teams
        val teamNames = listOf(
            "Arsenal", "Manchester City", "Liverpool", "Chelsea",
            "Manchester United", "Tottenham", "Newcastle", "Aston Villa",
            "Brighton", "West Ham",
        )

        val teamIds = teamNames.map { name ->
            teamRepository.insert(Team(leagueId = leagueId, name = name, shortName = name.take(3).uppercase()))
        }

        // 3. Matches (round-robin, ~45 matches)
        val now = Instant.now()
        var matchCount = 0
        val rng = Random(42) // deterministic seed

        for (i in teamIds.indices) {
            for (j in teamIds.indices) {
                if (i == j) continue

                val daysAgo = (rng.nextInt(7, 200)).toLong()
                val homeGoals = poissonSample(rng, 1.5)
                val awayGoals = poissonSample(rng, 1.2)

                val matchId = matchRepository.insert(
                    Match(
                        leagueId = leagueId,
                        homeTeamId = teamIds[i],
                        awayTeamId = teamIds[j],
                        matchDate = now.minus(daysAgo, ChronoUnit.DAYS),
                        stadium = "${teamNames[i]} Stadium",
                        homeScore = homeGoals,
                        awayScore = awayGoals,
                        status = "finished",
                    ),
                )

                // Stats for home team
                matchStatsRepository.insert(
                    MatchStats(
                        matchId = matchId,
                        teamId = teamIds[i],
                        possession = BigDecimal(rng.nextInt(40, 65).toString()),
                        shots = rng.nextInt(8, 20),
                        shotsOnTarget = rng.nextInt(2, 10),
                        corners = rng.nextInt(2, 12),
                        fouls = rng.nextInt(6, 18),
                        yellowCards = rng.nextInt(0, 4),
                        redCards = if (rng.nextInt(20) == 0) 1 else 0,
                        offsides = rng.nextInt(0, 5),
                        passes = rng.nextInt(350, 650),
                        passAccuracy = BigDecimal(rng.nextInt(72, 92).toString()),
                    ),
                )

                // Stats for away team
                matchStatsRepository.insert(
                    MatchStats(
                        matchId = matchId,
                        teamId = teamIds[j],
                        possession = BigDecimal((100 - rng.nextInt(40, 65)).toString()),
                        shots = rng.nextInt(5, 16),
                        shotsOnTarget = rng.nextInt(1, 8),
                        corners = rng.nextInt(1, 9),
                        fouls = rng.nextInt(7, 19),
                        yellowCards = rng.nextInt(0, 5),
                        redCards = if (rng.nextInt(25) == 0) 1 else 0,
                        offsides = rng.nextInt(0, 4),
                        passes = rng.nextInt(300, 600),
                        passAccuracy = BigDecimal(rng.nextInt(68, 90).toString()),
                    ),
                )

                matchCount++
            }
        }

        val msg = "Seeded: 1 league, ${teamNames.size} teams, $matchCount matches"
        log.info(msg)

        return msg
    }

    private fun poissonSample(rng: Random, lambda: Double): Int {
        val l = Math.exp(-lambda)
        var k = 0
        var p = 1.0
        do {
            k++
            p *= rng.nextDouble()
        } while (p > l)

        return k - 1
    }
}
