package com.sportanalytics.core.seed

import com.sportanalytics.core.model.*
import com.sportanalytics.core.repository.*
import org.slf4j.LoggerFactory
import java.math.BigDecimal
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

    fun seed(): String {
        if (leagueRepository.findAll().isNotEmpty()) {
            return "Database already seeded"
        }

        log.info("Seeding sample data...")

        val leagueId = leagueRepository.insert(
            League(name = "Premier League", country = "England", season = "2024-2025"),
        )

        val teamNames = listOf(
            "Arsenal", "Manchester City", "Liverpool", "Chelsea",
            "Manchester United", "Tottenham", "Newcastle", "Aston Villa",
            "Brighton", "West Ham",
        )

        val teamIds = teamNames.map { name ->
            teamRepository.insert(Team(leagueId = leagueId, name = name, shortName = name.take(3).uppercase()))
        }

        // Generate round-robin schedule: 9 rounds, 5 matches per round (= 45 matches total for half-season)
        val now = Instant.now()
        var matchCount = 0
        val rng = Random(42)

        // Build proper round-robin pairings
        val pairings = mutableListOf<Triple<Int, Int, Int>>() // (homeIdx, awayIdx, round)
        val n = teamIds.size // 10

        // Standard round-robin: 9 rounds for 10 teams, 5 matches each
        val teams = (0 until n).toMutableList()
        for (round in 1..9) {
            for (m in 0 until n / 2) {
                val home = teams[m]
                val away = teams[n - 1 - m]
                pairings.add(Triple(home, away, round))
            }
            // Rotate: fix first, rotate rest
            val last = teams.removeAt(teams.lastIndex)
            teams.add(1, last)
        }

        for ((homeIdx, awayIdx, round) in pairings) {
            val daysAgo = ((9 - round) * 14 + rng.nextInt(0, 7)).toLong()
            val homeGoals = poissonSample(rng, 1.5)
            val awayGoals = poissonSample(rng, 1.2)

            val matchId = matchRepository.insert(
                Match(
                    leagueId = leagueId,
                    homeTeamId = teamIds[homeIdx],
                    awayTeamId = teamIds[awayIdx],
                    matchDate = now.minus(daysAgo, ChronoUnit.DAYS),
                    stadium = "${teamNames[homeIdx]} Stadium",
                    homeScore = homeGoals,
                    awayScore = awayGoals,
                    status = "finished",
                    round = round,
                ),
            )

            matchStatsRepository.insert(
                MatchStats(
                    matchId = matchId, teamId = teamIds[homeIdx],
                    possession = BigDecimal(rng.nextInt(40, 65).toString()),
                    shots = rng.nextInt(8, 20), shotsOnTarget = rng.nextInt(2, 10),
                    corners = rng.nextInt(2, 12), fouls = rng.nextInt(6, 18),
                    yellowCards = rng.nextInt(0, 4),
                    redCards = if (rng.nextInt(20) == 0) 1 else 0,
                    offsides = rng.nextInt(0, 5),
                    passes = rng.nextInt(350, 650),
                    passAccuracy = BigDecimal(rng.nextInt(72, 92).toString()),
                ),
            )

            matchStatsRepository.insert(
                MatchStats(
                    matchId = matchId, teamId = teamIds[awayIdx],
                    possession = BigDecimal((100 - rng.nextInt(40, 65)).toString()),
                    shots = rng.nextInt(5, 16), shotsOnTarget = rng.nextInt(1, 8),
                    corners = rng.nextInt(1, 9), fouls = rng.nextInt(7, 19),
                    yellowCards = rng.nextInt(0, 5),
                    redCards = if (rng.nextInt(25) == 0) 1 else 0,
                    offsides = rng.nextInt(0, 4),
                    passes = rng.nextInt(300, 600),
                    passAccuracy = BigDecimal(rng.nextInt(68, 90).toString()),
                ),
            )

            matchCount++
        }

        val msg = "Seeded: 1 league, ${teamNames.size} teams, $matchCount matches (9 rounds)"
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
