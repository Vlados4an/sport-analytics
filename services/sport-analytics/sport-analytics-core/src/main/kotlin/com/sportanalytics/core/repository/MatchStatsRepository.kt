package com.sportanalytics.core.repository

import com.sportanalytics.core.model.MatchStats
import com.sportanalytics.jooq.tables.MatchStats.MATCH_STATS
import org.jooq.DSLContext
import org.jooq.Record

class MatchStatsRepository(private val dsl: DSLContext) {

    fun findByMatchId(matchId: Int): List<MatchStats> =
        dsl.selectFrom(MATCH_STATS).where(MATCH_STATS.MATCH_ID.eq(matchId)).fetch().map { mapStats(it) }

    fun findByMatchAndTeam(matchId: Int, teamId: Int): MatchStats? =
        dsl.selectFrom(MATCH_STATS)
            .where(MATCH_STATS.MATCH_ID.eq(matchId))
            .and(MATCH_STATS.TEAM_ID.eq(teamId))
            .fetchOne()
            ?.let { mapStats(it) }

    fun insert(stats: MatchStats): Int =
        dsl.insertInto(MATCH_STATS)
            .set(MATCH_STATS.MATCH_ID, stats.matchId)
            .set(MATCH_STATS.TEAM_ID, stats.teamId)
            .set(MATCH_STATS.POSSESSION, stats.possession)
            .set(MATCH_STATS.SHOTS, stats.shots)
            .set(MATCH_STATS.SHOTS_ON_TARGET, stats.shotsOnTarget)
            .set(MATCH_STATS.CORNERS, stats.corners)
            .set(MATCH_STATS.FOULS, stats.fouls)
            .set(MATCH_STATS.YELLOW_CARDS, stats.yellowCards)
            .set(MATCH_STATS.RED_CARDS, stats.redCards)
            .set(MATCH_STATS.OFFSIDES, stats.offsides)
            .set(MATCH_STATS.PASSES, stats.passes)
            .set(MATCH_STATS.PASS_ACCURACY, stats.passAccuracy)
            .returningResult(MATCH_STATS.ID)
            .fetchOne()!!
            .getValue(MATCH_STATS.ID)

    private fun mapStats(r: Record) = MatchStats(
        id = r[MATCH_STATS.ID], matchId = r[MATCH_STATS.MATCH_ID], teamId = r[MATCH_STATS.TEAM_ID],
        possession = r[MATCH_STATS.POSSESSION], shots = r[MATCH_STATS.SHOTS],
        shotsOnTarget = r[MATCH_STATS.SHOTS_ON_TARGET], corners = r[MATCH_STATS.CORNERS],
        fouls = r[MATCH_STATS.FOULS], yellowCards = r[MATCH_STATS.YELLOW_CARDS],
        redCards = r[MATCH_STATS.RED_CARDS], offsides = r[MATCH_STATS.OFFSIDES],
        passes = r[MATCH_STATS.PASSES], passAccuracy = r[MATCH_STATS.PASS_ACCURACY],
    )
}
