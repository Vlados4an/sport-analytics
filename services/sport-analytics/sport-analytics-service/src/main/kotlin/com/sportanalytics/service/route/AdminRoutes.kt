package com.sportanalytics.service.route

import com.sportanalytics.core.rating.RatingService
import com.sportanalytics.core.repository.LeagueRepository
import com.sportanalytics.core.seed.SeedDataService
import com.sportanalytics.di.justInstance
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI

data class RecalculateRequest(val leagueId: Int? = null)

fun Route.adminRoutes(di: DI) {
    val ratingService = di.justInstance<RatingService>()
    val seedService = di.justInstance<SeedDataService>()
    val leagueRepo = di.justInstance<LeagueRepository>()

    route("/api/admin") {
        post("/seed") {
            val result = seedService.seed()
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
            call.respond(mapOf("status" to "ratings recalculated", "leagueId" to leagueId))
        }
    }
}
