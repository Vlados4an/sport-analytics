package com.sportanalytics.service.route

import com.fasterxml.jackson.databind.ObjectMapper
import com.sportanalytics.core.infrastructure.RedisCache
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
    val cache = di.justInstance<RedisCache>()
    val mapper = di.justInstance<ObjectMapper>()

    route("/api/ratings") {
        get {
            val lid = call.request.queryParameters["league_id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "league_id required")

            // Cache-aside (diploma 5.2): serve from Redis, fall back to PostgreSQL
            val cacheKey = "ratings:league:$lid"
            cache.get(cacheKey)?.let {
                return@get call.respondText(it, ContentType.Application.Json)
            }

            val ratings = repo.findByLeagueId(lid)
            val teamIds = ratings.map { it.teamId }.distinct()
            val teamNames = teamIds.mapNotNull { id -> teamRepo.findById(id)?.let { id to it.name } }.toMap()

            val payload = ratings.map { r ->
                mapOf(
                    "teamId" to r.teamId, "teamName" to teamNames[r.teamId],
                    "leagueId" to r.leagueId, "rating" to r.rating,
                    "attackRating" to r.attackRating, "defenseRating" to r.defenseRating,
                    "form" to r.form,
                )
            }

            val json = mapper.writeValueAsString(payload)
            cache.set(cacheKey, json)
            call.respondText(json, ContentType.Application.Json)
        }

        get("/top") {
            val lid = call.request.queryParameters["league_id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "league_id required")
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 5

            val top = repo.findByLeagueId(lid)
                .sortedByDescending { it.rating }
                .take(limit)

            call.respond(top.map { r ->
                mapOf(
                    "teamId" to r.teamId,
                    "teamName" to teamRepo.findById(r.teamId)?.name,
                    "rating" to r.rating,
                )
            })
        }
    }
}
