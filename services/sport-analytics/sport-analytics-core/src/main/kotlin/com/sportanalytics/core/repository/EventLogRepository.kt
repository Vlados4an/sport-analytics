package com.sportanalytics.core.repository

import com.sportanalytics.core.model.EventLogEntry
import com.sportanalytics.jooq.tables.EventLog.EVENT_LOG
import org.jooq.DSLContext
import org.jooq.Record

class EventLogRepository(private val dsl: DSLContext) {

    fun insert(entry: EventLogEntry): Int =
        dsl.insertInto(EVENT_LOG)
            .set(EVENT_LOG.EVENT_TYPE, entry.eventType)
            .set(EVENT_LOG.MESSAGE, entry.message)
            .set(EVENT_LOG.STATUS, entry.status)
            .returningResult(EVENT_LOG.ID)
            .fetchOne()!!
            .getValue(EVENT_LOG.ID)

    fun findRecent(limit: Int = 50): List<EventLogEntry> =
        dsl.selectFrom(EVENT_LOG)
            .orderBy(EVENT_LOG.CREATED_AT.desc(), EVENT_LOG.ID.desc())
            .limit(limit)
            .fetch()
            .map { mapEntry(it) }

    private fun mapEntry(r: Record) = EventLogEntry(
        id = r[EVENT_LOG.ID],
        eventType = r[EVENT_LOG.EVENT_TYPE],
        message = r[EVENT_LOG.MESSAGE],
        status = r[EVENT_LOG.STATUS],
        createdAt = r[EVENT_LOG.CREATED_AT],
    )
}
