package com.sportanalytics.core.seed

import com.sportanalytics.core.model.MatchStats
import java.math.BigDecimal
import kotlin.math.exp
import kotlin.random.Random

/**
 * Simulated external data source: produces realistic match results and statistics
 * for the ETL pipeline and the initial seed. Scores follow a Poisson process
 * parameterized by team attack/defense strengths with home advantage.
 */
class MatchSimulator(private val rng: Random) {

    data class SimulatedResult(
        val homeGoals: Int,
        val awayGoals: Int,
        val referee: String,
        val attendance: Int,
        val homeStats: StatsDraft,
        val awayStats: StatsDraft,
    )

    data class StatsDraft(
        val possession: Int,
        val shots: Int,
        val shotsOnTarget: Int,
        val corners: Int,
        val fouls: Int,
        val yellowCards: Int,
        val redCards: Int,
        val offsides: Int,
        val passes: Int,
        val passAccuracy: Int,
    )

    fun simulate(home: EplTeam, away: EplTeam): SimulatedResult {
        val lambdaHome = (home.attack * away.defense * HOME_ADVANTAGE).coerceIn(0.3, 4.0)
        val lambdaAway = (away.attack * home.defense * AWAY_FACTOR).coerceIn(0.2, 3.5)

        val homeGoals = poisson(lambdaHome)
        val awayGoals = poisson(lambdaAway)

        val homePossession = (50 + ((home.attack - away.attack) * 8) + rng.nextInt(-6, 7))
            .toInt().coerceIn(28, 72)

        return SimulatedResult(
            homeGoals = homeGoals,
            awayGoals = awayGoals,
            referee = EplData.REFEREES[rng.nextInt(EplData.REFEREES.size)],
            attendance = (home.capacity * (0.86 + rng.nextDouble() * 0.13)).toInt(),
            homeStats = statsFor(homeGoals, homePossession, home.attack),
            awayStats = statsFor(awayGoals, 100 - homePossession, away.attack),
        )
    }

    fun toMatchStats(matchId: Int, teamId: Int, draft: StatsDraft) = MatchStats(
        matchId = matchId,
        teamId = teamId,
        possession = BigDecimal(draft.possession),
        shots = draft.shots,
        shotsOnTarget = draft.shotsOnTarget,
        corners = draft.corners,
        fouls = draft.fouls,
        yellowCards = draft.yellowCards,
        redCards = draft.redCards,
        offsides = draft.offsides,
        passes = draft.passes,
        passAccuracy = BigDecimal(draft.passAccuracy),
    )

    private fun statsFor(goals: Int, possession: Int, attack: Double): StatsDraft {
        val shots = (goals * 2 + (attack * 5).toInt() + rng.nextInt(2, 7)).coerceIn(3, 28)
        val onTarget = (goals + rng.nextInt(1, (shots / 2).coerceAtLeast(2))).coerceIn(goals, shots)

        return StatsDraft(
            possession = possession,
            shots = shots,
            shotsOnTarget = onTarget,
            corners = (shots / 2 + rng.nextInt(0, 4)).coerceIn(0, 14),
            fouls = rng.nextInt(6, 18),
            yellowCards = rng.nextInt(0, 5),
            redCards = if (rng.nextInt(22) == 0) 1 else 0,
            offsides = rng.nextInt(0, 6),
            passes = (possession * 9 + rng.nextInt(-40, 80)).coerceIn(220, 780),
            passAccuracy = (68 + possession / 4 + rng.nextInt(0, 6)).coerceIn(65, 93),
        )
    }

    private fun poisson(lambda: Double): Int {
        val l = exp(-lambda)
        var k = 0
        var p = 1.0
        do {
            k++
            p *= rng.nextDouble()
        } while (p > l)

        return k - 1
    }

    companion object {
        const val HOME_ADVANTAGE = 0.72
        const val AWAY_FACTOR = 0.55
    }
}
