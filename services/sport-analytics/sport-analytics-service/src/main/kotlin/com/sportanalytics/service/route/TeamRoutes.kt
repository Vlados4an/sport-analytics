package com.sportanalytics.service.route

import com.sportanalytics.core.repository.TeamRepository
import com.sportanalytics.core.repository.TeamRatingRepository
import com.sportanalytics.di.justInstance
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI

fun Route.teamRoutes(di: DI) {
    val repo = di.justInstance<TeamRepository>()
    val ratingRepo = di.justInstance<TeamRatingRepository>()

    route("/api/teams") {
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            call.respond(repo.findById(id) ?: return@get call.respond(HttpStatusCode.NotFound, "Not found"))
        }

        get("/{id}/rating") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            call.respond(ratingRepo.findLatestByTeamId(id) ?: return@get call.respond(HttpStatusCode.NotFound, "Not found"))
        }
    }
}
