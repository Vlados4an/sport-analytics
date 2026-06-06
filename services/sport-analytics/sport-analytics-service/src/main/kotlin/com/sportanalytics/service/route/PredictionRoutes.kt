package com.sportanalytics.service.route

import com.sportanalytics.core.prediction.AttackDefenseStrength
import com.sportanalytics.core.prediction.DixonColesModel
import com.sportanalytics.core.repository.MatchRepository
import com.sportanalytics.core.repository.PredictionRepository
import com.sportanalytics.di.justInstance
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.Instant

data class PredictRequest(val homeTeamId: Int, val awayTeamId: Int, val matchId: Int? = null)

fun Route.predictionRoutes(di: DI) {
    val matchRepo = di.justInstance<MatchRepository>()
    val predRepo = di.justInstance<PredictionRepository>()
    val model = di.justInstance<DixonColesModel>()

    route("/api/predictions") {
        post("/calculate") {
            val body = call.receive<PredictRequest>()
            val now = Instant.now()

            val homeMatches = matchRepo.findRecentByTeamId(body.homeTeamId, 20)
            val awayMatches = matchRepo.findRecentByTeamId(body.awayTeamId, 20)

            val leagueAvg = AttackDefenseStrength.LeagueAverages(1.35, 1.35, 1.52, 1.18)

            fun buildData(teamId: Int, matches: List<com.sportanalytics.core.model.Match>): List<AttackDefenseStrength.TeamMatchData> =
                matches.map { m ->
                    val days = Duration.between(m.matchDate, now).toDays().coerceAtLeast(0)
                    val isHome = m.homeTeamId == teamId
                    AttackDefenseStrength.TeamMatchData(
                        xg = (if (isHome) m.homeScore else m.awayScore)?.toDouble() ?: 1.3,
                        xga = (if (isHome) m.awayScore else m.homeScore)?.toDouble() ?: 1.3,
                        daysAgo = days,
                    )
                }

            val result = if (homeMatches.isEmpty() && awayMatches.isEmpty()) {
                model.predictFromLambdaMu(1.52, 1.18)
            } else {
                model.predict(
                    DixonColesModel.TeamData(buildData(body.homeTeamId, homeMatches), 1.0),
                    DixonColesModel.TeamData(buildData(body.awayTeamId, awayMatches), 1.0),
                    leagueAvg,
                )
            }

            if (body.matchId != null) {
                predRepo.insert(
                    com.sportanalytics.core.model.Prediction(
                        matchId = body.matchId,
                        homeWinProb = BigDecimal(result.homeWinProb).setScale(3, RoundingMode.HALF_UP),
                        drawProb = BigDecimal(result.drawProb).setScale(3, RoundingMode.HALF_UP),
                        awayWinProb = BigDecimal(result.awayWinProb).setScale(3, RoundingMode.HALF_UP),
                        predictedHomeScore = result.predictedHomeScore,
                        predictedAwayScore = result.predictedAwayScore,
                    ),
                )
            }

            call.respond(
                mapOf(
                    "homeWinProb" to "%.3f".format(result.homeWinProb),
                    "drawProb" to "%.3f".format(result.drawProb),
                    "awayWinProb" to "%.3f".format(result.awayWinProb),
                    "predictedScore" to "${result.predictedHomeScore}:${result.predictedAwayScore}",
                    "lambdaHome" to "%.3f".format(result.lambdaHome),
                    "muAway" to "%.3f".format(result.muAway),
                    "model" to result.modelVersion,
                    "scoreMatrix" to result.scoreMatrix.map { row -> row.toList() },
                ),
            )
        }

        get("/{matchId}") {
            val mid = call.parameters["matchId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid matchId")

            call.respond(predRepo.findByMatchId(mid) ?: return@get call.respond(HttpStatusCode.NotFound, "Not found"))
        }
    }
}
