package com.sportanalytics.core.rating

import com.sportanalytics.core.model.TeamRating
import com.sportanalytics.core.repository.MatchRepository
import com.sportanalytics.core.repository.MatchStatsRepository
import com.sportanalytics.core.repository.TeamRatingRepository
import com.sportanalytics.core.repository.TeamRepository
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.exp

/**
 * Rating algorithm (diploma Лист 4):
 *   Ia = α₁·xG + α₂·Sot + α₃·Kf
 *   Id = β₁·xGA + β₂·Tck + β₃·Int
 *   R = w₁·Ia + w₂·Id + w₃·F → clamp(0, 100)
 */
class RatingService(
    private val teamRepository: TeamRepository,
    private val matchRepository: MatchRepository,
    private val matchStatsRepository: MatchStatsRepository,
    private val teamRatingRepository: TeamRatingRepository,
) {
    private val log = LoggerFactory.getLogger(RatingService::class.java)

    companion object {
        const val MATCH_COUNT = 10
        const val MIN_MATCHES = 3
        const val DEFAULT_RATING = 50.0
        const val W_ATTACK = 0.40
        const val W_DEFENSE = 0.35
        const val W_FORM = 0.25
        const val FORM_DECAY = 0.15
    }

    fun calculateForTeam(teamId: Int, leagueId: Int): TeamRating {
        val matches = matchRepository.findRecentByTeamId(teamId, MATCH_COUNT)

        if (matches.size < MIN_MATCHES) {
            return TeamRating(teamId = teamId, leagueId = leagueId, rating = BigDecimal(DEFAULT_RATING))
        }

        // Form: weighted win/draw/loss
        var formNum = 0.0
        var formDen = 0.0
        for ((i, m) in matches.withIndex()) {
            val w = exp(-FORM_DECAY * (matches.size - 1 - i).toDouble())
            val pts = when {
                m.homeScore == null || m.awayScore == null -> 1.0
                m.homeTeamId == teamId && m.homeScore > m.awayScore -> 3.0
                m.awayTeamId == teamId && m.awayScore > m.homeScore -> 3.0
                m.homeScore == m.awayScore -> 1.0
                else -> 0.0
            }
            formNum += w * pts
            formDen += w * 3.0
        }
        val form = if (formDen > 0) (formNum / formDen) * 100.0 else 50.0

        // Stats averages
        var soT = 0.0; var corners = 0.0; var fouls = 0.0; var cnt = 0
        for (m in matches) {
            val s = matchStatsRepository.findByMatchAndTeam(m.id, teamId)
            if (s != null) {
                soT += s.shotsOnTarget?.toDouble() ?: 0.0
                corners += s.corners?.toDouble() ?: 0.0
                fouls += s.fouls?.toDouble() ?: 0.0
                cnt++
            }
        }
        val avgSoT = if (cnt > 0) soT / cnt else 3.0
        val avgCorners = if (cnt > 0) corners / cnt else 4.0
        val avgFouls = if (cnt > 0) fouls / cnt else 12.0

        // Goals as xG proxy
        val goalsScored = matches.mapNotNull { if (it.homeTeamId == teamId) it.homeScore else it.awayScore }.average().takeIf { !it.isNaN() } ?: 1.3
        val goalsConceded = matches.mapNotNull { if (it.homeTeamId == teamId) it.awayScore else it.homeScore }.average().takeIf { !it.isNaN() } ?: 1.3

        // Normalize to 0-100
        val normXg = (goalsScored / 3.0 * 100.0).coerceIn(0.0, 100.0)
        val normXga = ((3.0 - goalsConceded) / 3.0 * 100.0).coerceIn(0.0, 100.0)
        val normSoT = (avgSoT / 8.0 * 100.0).coerceIn(0.0, 100.0)
        val normCorners = (avgCorners / 10.0 * 100.0).coerceIn(0.0, 100.0)
        val normFouls = ((20.0 - avgFouls) / 20.0 * 100.0).coerceIn(0.0, 100.0)

        val attackIndex = 0.50 * normXg + 0.30 * normSoT + 0.20 * normCorners
        val defenseIndex = 0.50 * normXga + 0.25 * normFouls + 0.25 * normXga

        val rating = (W_ATTACK * attackIndex + W_DEFENSE * defenseIndex + W_FORM * form).coerceIn(0.0, 100.0)

        return TeamRating(
            teamId = teamId,
            leagueId = leagueId,
            rating = BigDecimal(rating).setScale(2, RoundingMode.HALF_UP),
            attackRating = BigDecimal(attackIndex).setScale(2, RoundingMode.HALF_UP),
            defenseRating = BigDecimal(defenseIndex).setScale(2, RoundingMode.HALF_UP),
            form = BigDecimal(form).setScale(2, RoundingMode.HALF_UP),
        )
    }

    fun recalculateAll(leagueId: Int) {
        val teams = teamRepository.findByLeagueId(leagueId)
        log.info("Recalculating ratings for {} teams in league {}", teams.size, leagueId)

        for (team in teams) {
            val rating = calculateForTeam(team.id, leagueId)
            teamRatingRepository.insert(rating)
            log.debug("Team {}: R={}", team.name, rating.rating)
        }

        log.info("Ratings recalculated for league {}", leagueId)
    }
}
