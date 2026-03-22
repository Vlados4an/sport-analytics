package com.sportanalytics.core.prediction

import com.sportanalytics.core.model.PredictionResult

class DixonColesModel(
    private val rho: Double = -0.07,
    private val xi: Double = TimeDecay.DEFAULT_XI,
    private val maxGoals: Int = ScoreMatrix.MAX_GOALS,
) {
    companion object {
        const val MODEL_VERSION: String = "dixon-coles-xg-v1"
    }

    data class TeamData(
        val matches: List<AttackDefenseStrength.TeamMatchData>,
        val formFactor: Double = 1.0,
    )

    fun predict(
        homeTeam: TeamData,
        awayTeam: TeamData,
        leagueAvg: AttackDefenseStrength.LeagueAverages,
    ): PredictionResult {
        val homeStrength = AttackDefenseStrength.calculate(homeTeam.matches, leagueAvg, xi)
        val awayStrength = AttackDefenseStrength.calculate(awayTeam.matches, leagueAvg, xi)

        val lambda = leagueAvg.avgHomeGoals *
            homeStrength.attackStrength * awayStrength.defenseStrength * homeTeam.formFactor
        val mu = leagueAvg.avgAwayGoals *
            awayStrength.attackStrength * homeStrength.defenseStrength * awayTeam.formFactor

        return predictFromLambdaMu(lambda, mu)
    }

    fun predictFromLambdaMu(lambda: Double, mu: Double): PredictionResult {
        val l = lambda.coerceIn(0.1, 6.0)
        val m = mu.coerceIn(0.1, 6.0)
        val matrix = ScoreMatrix.buildCorrectedMatrix(l, m, rho, maxGoals)
        val outcomes = ScoreMatrix.outcomeProbabilities(matrix)
        val best = ScoreMatrix.mostProbableScore(matrix)

        return PredictionResult(
            homeWinProb = outcomes.homeWin,
            drawProb = outcomes.draw,
            awayWinProb = outcomes.awayWin,
            predictedHomeScore = best.homeGoals,
            predictedAwayScore = best.awayGoals,
            scoreMatrix = matrix,
            lambdaHome = l,
            muAway = m,
            modelVersion = MODEL_VERSION,
        )
    }
}
