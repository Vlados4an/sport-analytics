package com.sportanalytics.core.repository

import com.sportanalytics.core.model.Team
import com.sportanalytics.jooq.tables.Teams.TEAMS
import org.jooq.DSLContext
import org.jooq.Record

class TeamRepository(private val dsl: DSLContext) {

    fun findByLeagueId(leagueId: Int): List<Team> =
        dsl.selectFrom(TEAMS).where(TEAMS.LEAGUE_ID.eq(leagueId)).orderBy(TEAMS.NAME).fetch().map { mapTeam(it) }

    fun findById(id: Int): Team? =
        dsl.selectFrom(TEAMS).where(TEAMS.ID.eq(id)).fetchOne()?.let { mapTeam(it) }

    fun findByApiId(apiId: Int): Team? =
        dsl.selectFrom(TEAMS).where(TEAMS.API_ID.eq(apiId)).fetchOne()?.let { mapTeam(it) }

    fun insert(team: Team): Int =
        dsl.insertInto(TEAMS)
            .set(TEAMS.LEAGUE_ID, team.leagueId)
            .set(TEAMS.NAME, team.name)
            .set(TEAMS.SHORT_NAME, team.shortName)
            .set(TEAMS.LOGO_URL, team.logoUrl)
            .set(TEAMS.API_ID, team.apiId)
            .returningResult(TEAMS.ID)
            .fetchOne()!!
            .getValue(TEAMS.ID)

    private fun mapTeam(r: Record) = Team(
        id = r[TEAMS.ID], leagueId = r[TEAMS.LEAGUE_ID], name = r[TEAMS.NAME],
        shortName = r[TEAMS.SHORT_NAME], logoUrl = r[TEAMS.LOGO_URL],
        apiId = r[TEAMS.API_ID], createdAt = r[TEAMS.CREATED_AT],
    )
}
