package com.sportanalytics.core.repository

import com.sportanalytics.core.model.TeamRating
import com.sportanalytics.jooq.tables.TeamRatings.TEAM_RATINGS
import org.jooq.DSLContext
import org.jooq.Record

class TeamRatingRepository(private val dsl: DSLContext) {

    fun findLatestByTeamId(teamId: Int): TeamRating? =
        dsl.selectFrom(TEAM_RATINGS)
            .where(TEAM_RATINGS.TEAM_ID.eq(teamId))
            .orderBy(TEAM_RATINGS.CALCULATED_AT.desc())
            .limit(1)
            .fetchOne()
            ?.let { mapRating(it) }

    fun findByLeagueId(leagueId: Int): List<TeamRating> =
        dsl.selectFrom(TEAM_RATINGS)
            .where(TEAM_RATINGS.LEAGUE_ID.eq(leagueId))
            .orderBy(TEAM_RATINGS.RATING.desc())
            .fetch()
            .map { mapRating(it) }

    fun insert(rating: TeamRating): Int =
        dsl.insertInto(TEAM_RATINGS)
            .set(TEAM_RATINGS.TEAM_ID, rating.teamId)
            .set(TEAM_RATINGS.LEAGUE_ID, rating.leagueId)
            .set(TEAM_RATINGS.RATING, rating.rating)
            .set(TEAM_RATINGS.ATTACK_RATING, rating.attackRating)
            .set(TEAM_RATINGS.DEFENSE_RATING, rating.defenseRating)
            .set(TEAM_RATINGS.FORM, rating.form)
            .returningResult(TEAM_RATINGS.ID)
            .fetchOne()!!
            .getValue(TEAM_RATINGS.ID)

    private fun mapRating(r: Record) = TeamRating(
        id = r[TEAM_RATINGS.ID], teamId = r[TEAM_RATINGS.TEAM_ID], leagueId = r[TEAM_RATINGS.LEAGUE_ID],
        rating = r[TEAM_RATINGS.RATING], attackRating = r[TEAM_RATINGS.ATTACK_RATING],
        defenseRating = r[TEAM_RATINGS.DEFENSE_RATING], form = r[TEAM_RATINGS.FORM],
        calculatedAt = r[TEAM_RATINGS.CALCULATED_AT],
    )
}
