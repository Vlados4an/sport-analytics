package com.sportanalytics.core.model

import java.time.Instant

/**
 * Analytical metrics row stored in ClickHouse match_metrics (diploma table 4.9).
 */
data class MatchMetrics(
    val matchId: Int,
    val teamId: Int,
    val matchDate: Instant,
    val xg: Double,
    val xa: Double,
    val xga: Double,
    val attackIndex: Double,
    val defenceIndex: Double,
)
