package com.sportanalytics.core.model

import java.time.Instant

data class Match(
    val id: Int = 0,
    val leagueId: Int,
    val homeTeamId: Int,
    val awayTeamId: Int,
    val matchDate: Instant,
    val stadium: String? = null,
    val attendance: Int? = null,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val status: String = "scheduled",
    val round: Int? = null,
    val referee: String? = null,
    val statsLoaded: Boolean = false,
    val metricsCalculated: Boolean = false,
    val apiId: Int? = null,
    val createdAt: Instant = Instant.now(),
)
