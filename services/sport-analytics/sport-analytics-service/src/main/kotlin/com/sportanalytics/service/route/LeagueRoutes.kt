package com.sportanalytics.service.route

import com.sportanalytics.core.repository.LeagueRepository
import com.sportanalytics.core.repository.MatchRepository
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
    val matchRepo = di.justInstance<MatchRepository>()

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

        get("/{id}/standings") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            val teams = teamRepo.findByLeagueId(id).associateBy { it.id }
            val finished = matchRepo.findByLeagueId(id).filter { it.status == "finished" }

            data class Row(
                var played: Int = 0, var won: Int = 0, var drawn: Int = 0, var lost: Int = 0,
                var goalsFor: Int = 0, var goalsAgainst: Int = 0, var points: Int = 0,
            )

            val table = teams.keys.associateWith { Row() }
            for (m in finished) {
                val hs = m.homeScore ?: continue
                val asc = m.awayScore ?: continue
                val home = table[m.homeTeamId] ?: continue
                val away = table[m.awayTeamId] ?: continue

                home.played++; away.played++
                home.goalsFor += hs; home.goalsAgainst += asc
                away.goalsFor += asc; away.goalsAgainst += hs
                when {
                    hs > asc -> { home.won++; home.points += 3; away.lost++ }
                    hs < asc -> { away.won++; away.points += 3; home.lost++ }
                    else -> { home.drawn++; away.drawn++; home.points++; away.points++ }
                }
            }

            val standings = table.entries
                .sortedWith(
                    compareByDescending<Map.Entry<Int, Row>> { it.value.points }
                        .thenByDescending { it.value.goalsFor - it.value.goalsAgainst }
                        .thenByDescending { it.value.goalsFor },
                )
                .mapIndexed { idx, (teamId, row) ->
                    mapOf(
                        "position" to idx + 1,
                        "teamId" to teamId,
                        "teamName" to teams[teamId]?.name,
                        "played" to row.played,
                        "won" to row.won,
                        "drawn" to row.drawn,
                        "lost" to row.lost,
                        "goalsFor" to row.goalsFor,
                        "goalsAgainst" to row.goalsAgainst,
                        "goalDifference" to (row.goalsFor - row.goalsAgainst),
                        "points" to row.points,
                    )
                }

            call.respond(standings)
        }
    }
}
