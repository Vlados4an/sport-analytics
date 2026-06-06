package com.sportanalytics.service.route

import com.sportanalytics.core.repository.TeamRatingRepository
import com.sportanalytics.core.repository.TeamRepository
import com.sportanalytics.di.justInstance
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI

fun Route.ratingRoutes(di: DI) {
    val repo = di.justInstance<TeamRatingRepository>()
    val teamRepo = di.justInstance<TeamRepository>()

    route("/api/ratings") {
        get {
            val lid = call.request.queryParameters["league_id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "league_id required")

            val ratings = repo.findByLeagueId(lid)
            val teamIds = ratings.map { it.teamId }.distinct()
            val teamNames = teamIds.mapNotNull { id -> teamRepo.findById(id)?.let { id to it.name } }.toMap()

            call.respond(ratings.map { r ->
                mapOf(
                    "teamId" to r.teamId, "teamName" to teamNames[r.teamId],
                    "leagueId" to r.leagueId, "rating" to r.rating,
                    "attackRating" to r.attackRating, "defenseRating" to r.defenseRating,
                    "form" to r.form,
                )
            })
        }
    }
}
