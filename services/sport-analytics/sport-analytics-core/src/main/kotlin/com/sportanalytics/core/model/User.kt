package com.sportanalytics.core.model

import java.time.Instant

data class User(
    val id: Int = 0,
    val login: String,
    val passwordHash: String,
    val role: String = "user",
    val createdAt: Instant = Instant.now(),
)
