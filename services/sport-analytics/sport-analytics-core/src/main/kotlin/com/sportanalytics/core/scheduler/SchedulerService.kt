package com.sportanalytics.core.scheduler

import com.sportanalytics.core.etl.ETLService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicReference

/**
 * Task scheduler (diploma 3.3): triggers the ETL pipeline daily.
 * Disabled by default for the demo environment — runs are triggered
 * on demand through POST /api/admin/etl/run.
 */
class SchedulerService(private val etlService: ETLService) {

    private val log = LoggerFactory.getLogger(SchedulerService::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var job: Job? = null
    private val lastRun = AtomicReference<Instant?>(null)
    private val lastResult = AtomicReference<String?>(null)

    @Volatile
    var enabled: Boolean = false
        private set

    fun start() {
        if (job != null) return

        enabled = true
        job = scope.launch {
            while (true) {
                delay(untilNextRun().toMillis())
                runOnDemand()
            }
        }
        log.info("Scheduler started: daily ETL at {} UTC", DAILY_RUN_TIME)
    }

    fun runOnDemand(): String {
        val result = etlService.runPipeline()
        lastRun.set(Instant.now())
        lastResult.set(result.message)

        return result.message
    }

    fun status(): Map<String, Any?> = mapOf(
        "enabled" to enabled,
        "schedule" to "daily at $DAILY_RUN_TIME UTC",
        "lastRun" to lastRun.get()?.toString(),
        "lastResult" to lastResult.get(),
    )

    private fun untilNextRun(): Duration {
        val now = Instant.now()
        var next = LocalDate.now(ZoneOffset.UTC).atTime(DAILY_RUN_TIME).toInstant(ZoneOffset.UTC)

        if (!next.isAfter(now)) {
            next = next.plus(Duration.ofDays(1))
        }

        return Duration.between(now, next)
    }

    companion object {
        val DAILY_RUN_TIME: LocalTime = LocalTime.of(3, 0)
    }
}
