package com.sportanalytics.service.route

import com.sportanalytics.core.repository.MatchRepository
import com.sportanalytics.core.repository.MatchStatsRepository
import com.sportanalytics.di.justInstance
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import java.time.Instant

fun Route.matchRoutes(di: DI) {
    val repo = di.justInstance<MatchRepository>()
    val statsRepo = di.justInstance<MatchStatsRepository>()

    route("/api/matches") {
        get {
            val lid = call.request.queryParameters["league_id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "league_id required")

            val from = call.request.queryParameters["date_from"]?.let { Instant.parse(it) }
            val to = call.request.queryParameters["date_to"]?.let { Instant.parse(it) }

            call.respond(repo.findByLeagueId(lid, from, to))
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            call.respond(repo.findById(id) ?: return@get call.respond(HttpStatusCode.NotFound, "Not found"))
        }

        get("/{id}/stats") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            call.respond(statsRepo.findByMatchId(id))
        }
    }
}
