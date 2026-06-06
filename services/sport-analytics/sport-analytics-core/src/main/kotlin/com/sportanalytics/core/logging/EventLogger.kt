package com.sportanalytics.core.logging

import com.sportanalytics.core.model.EventLogEntry
import com.sportanalytics.core.repository.EventLogRepository
import org.slf4j.LoggerFactory

/**
 * System event journal (diploma table event_log): external API calls,
 * validation failures, ETL results and rating recalculations.
 */
class EventLogger(private val eventLogRepository: EventLogRepository) {

    private val log = LoggerFactory.getLogger(EventLogger::class.java)

    fun info(type: EventType, message: String) = write(type, message, "ok")

    fun error(type: EventType, message: String) = write(type, message, "error")

    private fun write(type: EventType, message: String, status: String) {
        try {
            eventLogRepository.insert(EventLogEntry(eventType = type.name, message = message, status = status))
        } catch (e: Exception) {
            log.error("Failed to write event log entry {}", type, e)
        }
    }

    enum class EventType {
        API_CALL,
        VALIDATION,
        ETL,
        SYNC,
        RATING,
        SEED,
        CACHE,
        AUTH,
    }
}
