package com.sportanalytics.service.route

import com.sportanalytics.core.repository.TeamRatingRepository
import com.sportanalytics.di.justInstance
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI

fun Route.ratingRoutes(di: DI) {
    val repo = di.justInstance<TeamRatingRepository>()

    route("/api/ratings") {
        get {
            val lid = call.request.queryParameters["league_id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "league_id required")

            call.respond(repo.findByLeagueId(lid))
        }
    }
}
