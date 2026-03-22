package com.sportanalytics.core.model

import java.time.Instant

data class Team(
    val id: Int = 0,
    val leagueId: Int,
    val name: String,
    val shortName: String? = null,
    val logoUrl: String? = null,
    val apiId: Int? = null,
    val createdAt: Instant = Instant.now(),
)
