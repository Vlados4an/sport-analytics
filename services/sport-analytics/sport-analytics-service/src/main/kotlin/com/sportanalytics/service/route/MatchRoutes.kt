package com.sportanalytics.service.route

import com.sportanalytics.core.repository.MatchRepository
import com.sportanalytics.core.repository.MatchStatsRepository
import com.sportanalytics.core.repository.TeamRepository
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
    val teamRepo = di.justInstance<TeamRepository>()

    route("/api/matches") {
        get {
            val lid = call.request.queryParameters["league_id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "league_id required")

            val from = call.request.queryParameters["date_from"]?.let { Instant.parse(it) }
            val to = call.request.queryParameters["date_to"]?.let { Instant.parse(it) }

            val matches = repo.findByLeagueId(lid, from, to)
            val teamIds = matches.flatMap { listOf(it.homeTeamId, it.awayTeamId) }.distinct()
            val teamNames = teamIds.mapNotNull { id -> teamRepo.findById(id)?.let { id to it.name } }.toMap()

            call.respond(matches.map { m ->
                mapOf(
                    "id" to m.id, "leagueId" to m.leagueId,
                    "homeTeamId" to m.homeTeamId, "awayTeamId" to m.awayTeamId,
                    "homeTeamName" to teamNames[m.homeTeamId],
                    "awayTeamName" to teamNames[m.awayTeamId],
                    "homeScore" to m.homeScore, "awayScore" to m.awayScore,
                    "matchDate" to m.matchDate.toString(), "round" to (m.round ?: 0),
                    "status" to m.status.uppercase(), "stadium" to m.stadium,
                    "attendance" to m.attendance,
                )
            })
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            val m = repo.findById(id) ?: return@get call.respond(HttpStatusCode.NotFound, "Not found")
            val homeName = teamRepo.findById(m.homeTeamId)?.name
            val awayName = teamRepo.findById(m.awayTeamId)?.name

            call.respond(mapOf(
                "id" to m.id, "leagueId" to m.leagueId,
                "homeTeamId" to m.homeTeamId, "awayTeamId" to m.awayTeamId,
                "homeTeamName" to homeName, "awayTeamName" to awayName,
                "homeScore" to m.homeScore, "awayScore" to m.awayScore,
                "matchDate" to m.matchDate.toString(), "round" to (m.round ?: 0),
                "status" to m.status.uppercase(), "stadium" to m.stadium,
                "attendance" to m.attendance,
            ))
        }

        get("/{id}/stats") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            call.respond(statsRepo.findByMatchId(id))
        }
    }
}
