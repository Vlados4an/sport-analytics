package com.sportanalytics.core.model

import java.time.Instant

data class EventLogEntry(
    val id: Int = 0,
    val eventType: String,
    val message: String? = null,
    val status: String = "info",
    val createdAt: Instant = Instant.now(),
)
