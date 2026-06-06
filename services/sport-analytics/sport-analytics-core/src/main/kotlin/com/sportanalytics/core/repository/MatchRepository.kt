package com.sportanalytics.core.repository

import com.sportanalytics.core.model.Match
import com.sportanalytics.jooq.tables.Matches.MATCHES
import org.jooq.DSLContext
import org.jooq.Record
import java.time.Instant

class MatchRepository(private val dsl: DSLContext) {

    fun findById(id: Int): Match? =
        dsl.selectFrom(MATCHES).where(MATCHES.ID.eq(id)).fetchOne()?.let { mapMatch(it) }

    fun findByLeagueId(leagueId: Int, from: Instant? = null, to: Instant? = null): List<Match> {
        var query = dsl.selectFrom(MATCHES).where(MATCHES.LEAGUE_ID.eq(leagueId))
        if (from != null) query = query.and(MATCHES.MATCH_DATE.ge(from))
        if (to != null) query = query.and(MATCHES.MATCH_DATE.le(to))

        return query.orderBy(MATCHES.MATCH_DATE.desc()).fetch().map { mapMatch(it) }
    }

    fun findRecentByTeamId(teamId: Int, limit: Int = 20): List<Match> =
        dsl.selectFrom(MATCHES)
            .where(MATCHES.HOME_TEAM_ID.eq(teamId).or(MATCHES.AWAY_TEAM_ID.eq(teamId)))
            .and(MATCHES.STATUS.eq("finished"))
            .orderBy(MATCHES.MATCH_DATE.desc())
            .limit(limit)
            .fetch()
            .map { mapMatch(it) }

    fun insert(match: Match): Int =
        dsl.insertInto(MATCHES)
            .set(MATCHES.LEAGUE_ID, match.leagueId)
            .set(MATCHES.HOME_TEAM_ID, match.homeTeamId)
            .set(MATCHES.AWAY_TEAM_ID, match.awayTeamId)
            .set(MATCHES.MATCH_DATE, match.matchDate)
            .set(MATCHES.STADIUM, match.stadium)
            .set(MATCHES.ATTENDANCE, match.attendance)
            .set(MATCHES.HOME_SCORE, match.homeScore)
            .set(MATCHES.AWAY_SCORE, match.awayScore)
            .set(MATCHES.STATUS, match.status)
            .set(MATCHES.ROUND, match.round)
            .set(MATCHES.REFEREE, match.referee)
            .set(MATCHES.STATS_LOADED, match.statsLoaded)
            .set(MATCHES.METRICS_CALCULATED, match.metricsCalculated)
            .set(MATCHES.API_ID, match.apiId)
            .returningResult(MATCHES.ID)
            .fetchOne()!!
            .getValue(MATCHES.ID)

    fun countByLeagueId(leagueId: Int): Int =
        dsl.selectCount().from(MATCHES).where(MATCHES.LEAGUE_ID.eq(leagueId)).fetchOne(0, Int::class.java) ?: 0

    fun maxRound(leagueId: Int): Int =
        dsl.select(org.jooq.impl.DSL.max(MATCHES.ROUND))
            .from(MATCHES)
            .where(MATCHES.LEAGUE_ID.eq(leagueId))
            .fetchOne(0, Int::class.java) ?: 0

    fun findScheduledByRound(leagueId: Int, round: Int): List<Match> =
        dsl.selectFrom(MATCHES)
            .where(MATCHES.LEAGUE_ID.eq(leagueId))
            .and(MATCHES.ROUND.eq(round))
            .and(MATCHES.STATUS.eq("scheduled"))
            .orderBy(MATCHES.MATCH_DATE.asc())
            .fetch()
            .map { mapMatch(it) }

    fun minScheduledRound(leagueId: Int): Int? =
        dsl.select(org.jooq.impl.DSL.min(MATCHES.ROUND))
            .from(MATCHES)
            .where(MATCHES.LEAGUE_ID.eq(leagueId))
            .and(MATCHES.STATUS.eq("scheduled"))
            .fetchOne(0, Int::class.java)

    fun applyResult(match: Match) {
        dsl.update(MATCHES)
            .set(MATCHES.HOME_SCORE, match.homeScore)
            .set(MATCHES.AWAY_SCORE, match.awayScore)
            .set(MATCHES.STATUS, match.status)
            .set(MATCHES.REFEREE, match.referee)
            .set(MATCHES.ATTENDANCE, match.attendance)
            .set(MATCHES.STATS_LOADED, match.statsLoaded)
            .where(MATCHES.ID.eq(match.id))
            .execute()
    }

    fun findPendingMetrics(leagueId: Int): List<Match> =
        dsl.selectFrom(MATCHES)
            .where(MATCHES.LEAGUE_ID.eq(leagueId))
            .and(MATCHES.STATUS.eq("finished"))
            .and(MATCHES.METRICS_CALCULATED.eq(false))
            .orderBy(MATCHES.MATCH_DATE.asc())
            .fetch()
            .map { mapMatch(it) }

    fun markMetricsCalculated(matchIds: List<Int>) {
        if (matchIds.isEmpty()) return

        dsl.update(MATCHES)
            .set(MATCHES.METRICS_CALCULATED, true)
            .where(MATCHES.ID.`in`(matchIds))
            .execute()
    }

    private fun mapMatch(r: Record) = Match(
        id = r[MATCHES.ID], leagueId = r[MATCHES.LEAGUE_ID],
        homeTeamId = r[MATCHES.HOME_TEAM_ID], awayTeamId = r[MATCHES.AWAY_TEAM_ID],
        matchDate = r[MATCHES.MATCH_DATE], stadium = r[MATCHES.STADIUM],
        attendance = r[MATCHES.ATTENDANCE], homeScore = r[MATCHES.HOME_SCORE],
        awayScore = r[MATCHES.AWAY_SCORE], status = r[MATCHES.STATUS],
        round = r[MATCHES.ROUND], referee = r[MATCHES.REFEREE],
        statsLoaded = r[MATCHES.STATS_LOADED] ?: false,
        metricsCalculated = r[MATCHES.METRICS_CALCULATED] ?: false,
        apiId = r[MATCHES.API_ID],
    )
}
