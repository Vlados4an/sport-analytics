package com.sportanalytics.core.model

import java.time.Instant

data class League(
    val id: Int = 0,
    val name: String,
    val country: String,
    val season: String,
    val apiId: Int? = null,
    val createdAt: Instant = Instant.now(),
)
