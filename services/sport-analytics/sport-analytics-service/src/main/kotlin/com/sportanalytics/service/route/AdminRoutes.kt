package com.sportanalytics.service.route

import com.sportanalytics.core.etl.ETLService
import com.sportanalytics.core.infrastructure.ClickHouseClient
import com.sportanalytics.core.infrastructure.RedisCache
import com.sportanalytics.core.logging.EventLogger
import com.sportanalytics.core.rating.RatingService
import com.sportanalytics.core.repository.EventLogRepository
import com.sportanalytics.core.repository.LeagueRepository
import com.sportanalytics.core.repository.MatchRepository
import com.sportanalytics.core.repository.TeamRepository
import com.sportanalytics.core.scheduler.SchedulerService
import com.sportanalytics.core.seed.SeedDataService
import com.sportanalytics.di.justInstance
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jooq.DSLContext
import org.kodein.di.DI

data class RecalculateRequest(val leagueId: Int? = null)

fun Route.adminRoutes(di: DI) {
    val ratingService = di.justInstance<RatingService>()
    val seedService = di.justInstance<SeedDataService>()
    val leagueRepo = di.justInstance<LeagueRepository>()
    val teamRepo = di.justInstance<TeamRepository>()
    val matchRepo = di.justInstance<MatchRepository>()
    val etlService = di.justInstance<ETLService>()
    val schedulerService = di.justInstance<SchedulerService>()
    val eventLogRepo = di.justInstance<EventLogRepository>()
    val eventLogger = di.justInstance<EventLogger>()
    val clickHouse = di.justInstance<ClickHouseClient>()
    val redisCache = di.justInstance<RedisCache>()
    val dsl = di.justInstance<DSLContext>()

    route("/api/admin") {
        post("/seed") {
            val result = seedService.seed()
            eventLogger.info(EventLogger.EventType.SEED, result)
            call.respond(mapOf("status" to result))
        }

        post("/recalculate-ratings") {
            val leagueId = try {
                call.receive<RecalculateRequest>().leagueId
            } catch (_: Exception) {
                null
            } ?: leagueRepo.findAll().firstOrNull()?.id
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No leagues found. Seed first."))

            ratingService.recalculateAll(leagueId)
            redisCache.invalidate("ratings:*")
            eventLogger.info(
                EventLogger.EventType.RATING,
                "Team ratings recalculated manually (R = 0.40·Ia + 0.35·Id + 0.25·F)",
            )
            call.respond(mapOf("status" to "ratings recalculated", "leagueId" to leagueId))
        }

        post("/etl/run") {
            val result = etlService.runPipeline()

            call.respond(
                mapOf(
                    "status" to result.message,
                    "round" to result.round,
                    "matchesLoaded" to result.matchesLoaded,
                    "statsLoaded" to result.statsLoaded,
                    "metricsRows" to result.metricsRows,
                ),
            )
        }

        get("/events") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 30

            call.respond(
                eventLogRepo.findRecent(limit).map { e ->
                    mapOf(
                        "id" to e.id,
                        "eventType" to e.eventType,
                        "message" to e.message,
                        "status" to e.status,
                        "createdAt" to e.createdAt.toString(),
                    )
                },
            )
        }

        get("/status") {
            val pgOnline = try {
                dsl.selectOne().fetch(); true
            } catch (_: Exception) {
                false
            }

            val leagueId = leagueRepo.findAll().firstOrNull()?.id
            val teamCount = leagueId?.let { teamRepo.findByLeagueId(it).size } ?: 0
            val matchCount = leagueId?.let { matchRepo.countByLeagueId(it) } ?: 0

            call.respond(
                mapOf(
                    "postgres" to mapOf("online" to pgOnline, "teams" to teamCount, "matches" to matchCount),
                    "clickhouse" to mapOf("online" to clickHouse.ping(), "metricsRows" to clickHouse.countMetrics()),
                    "redis" to mapOf("online" to redisCache.ping()),
                    "scheduler" to schedulerService.status(),
                ),
            )
        }

        post("/cache/clear") {
            val evicted = redisCache.invalidate("*")
            eventLogger.info(EventLogger.EventType.CACHE, "Cache cleared manually: $evicted keys evicted")
            call.respond(mapOf("status" to "cache cleared", "keysEvicted" to evicted))
        }
    }
}
