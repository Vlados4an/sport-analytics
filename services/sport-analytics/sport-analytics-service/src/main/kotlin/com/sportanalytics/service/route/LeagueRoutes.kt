package com.sportanalytics.service.route

import com.sportanalytics.core.repository.LeagueRepository
import com.sportanalytics.core.repository.TeamRepository
import com.sportanalytics.di.justInstance
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI

fun Route.leagueRoutes(di: DI) {
    val repo = di.justInstance<LeagueRepository>()
    val teamRepo = di.justInstance<TeamRepository>()

    route("/api/leagues") {
        get { call.respond(repo.findAll()) }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            call.respond(repo.findById(id) ?: return@get call.respond(HttpStatusCode.NotFound, "Not found"))
        }

        get("/{id}/teams") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            call.respond(teamRepo.findByLeagueId(id))
        }
    }
}
