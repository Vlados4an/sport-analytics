package com.sportanalytics.core.repository

import com.sportanalytics.core.model.League
import com.sportanalytics.jooq.tables.Leagues.LEAGUES
import org.jooq.DSLContext
import org.jooq.Record

class LeagueRepository(private val dsl: DSLContext) {

    fun findAll(): List<League> =
        dsl.selectFrom(LEAGUES).orderBy(LEAGUES.NAME).fetch().map { mapLeague(it) }

    fun findById(id: Int): League? =
        dsl.selectFrom(LEAGUES).where(LEAGUES.ID.eq(id)).fetchOne()?.let { mapLeague(it) }

    fun findByApiId(apiId: Int): League? =
        dsl.selectFrom(LEAGUES).where(LEAGUES.API_ID.eq(apiId)).fetchOne()?.let { mapLeague(it) }

    fun insert(league: League): Int =
        dsl.insertInto(LEAGUES)
            .set(LEAGUES.NAME, league.name)
            .set(LEAGUES.COUNTRY, league.country)
            .set(LEAGUES.SEASON, league.season)
            .set(LEAGUES.API_ID, league.apiId)
            .returningResult(LEAGUES.ID)
            .fetchOne()!!
            .getValue(LEAGUES.ID)

    private fun mapLeague(r: Record) = League(
        id = r[LEAGUES.ID], name = r[LEAGUES.NAME], country = r[LEAGUES.COUNTRY],
        season = r[LEAGUES.SEASON], apiId = r[LEAGUES.API_ID], createdAt = r[LEAGUES.CREATED_AT],
    )
}
