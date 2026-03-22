package com.sportanalytics.core.prediction

object AttackDefenseStrength {

    data class TeamMatchData(val xg: Double, val xga: Double, val daysAgo: Long)

    data class LeagueAverages(
        val avgXg: Double,
        val avgXga: Double,
        val avgHomeGoals: Double,
        val avgAwayGoals: Double,
    )

    data class StrengthResult(val attackStrength: Double, val defenseStrength: Double)

    fun calculate(
        matches: List<TeamMatchData>,
        leagueAvg: LeagueAverages,
        xi: Double = TimeDecay.DEFAULT_XI,
    ): StrengthResult {
        if (matches.isEmpty()) return StrengthResult(1.0, 1.0)

        val weights = matches.map { TimeDecay.weight(it.daysAgo, xi) }
        val totalWeight = weights.sum()

        val weightedXg = matches.zip(weights).sumOf { (m, w) -> m.xg * w }
        val weightedXga = matches.zip(weights).sumOf { (m, w) -> m.xga * w }

        val avgXg = weightedXg / totalWeight
        val avgXga = weightedXga / totalWeight

        val as_ = if (leagueAvg.avgXg > 0) avgXg / leagueAvg.avgXg else 1.0
        val ds = if (leagueAvg.avgXga > 0) avgXga / leagueAvg.avgXga else 1.0

        return StrengthResult(as_.coerceIn(0.1, 5.0), ds.coerceIn(0.1, 5.0))
    }
}
