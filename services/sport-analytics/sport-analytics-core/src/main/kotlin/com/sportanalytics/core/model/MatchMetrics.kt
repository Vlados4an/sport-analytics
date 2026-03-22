package com.sportanalytics.core.model

data class MatchMetrics(
    val matchId: Long,
    val teamId: Long,
    val xg: Double,
    val xa: Double,
    val xga: Double,
    val attackIndex: Double,
    val defenseIndex: Double,
    val form: Double,
    val rating: Double,
)
