---
name: kotlin-be-create-dao
description: Create a DAO with JOOQ for Kotlin backend. Use when creating DAO, database access, JOOQ queries.
internal: true
allowed-tools: Read, Grep, Glob, Edit, Write
---

# Create a DAO (JOOQ)

## Location
`sport-analytics-core/src/main/kotlin/com/sportanalytics/core/repository/<Name>Dao.kt`

## Pattern

```kotlin
class LeagueDao(
    private val dsl: DSLContext,
) {
    fun findById(id: Int): League? =
        dsl.selectFrom(LEAGUES)
            .where(LEAGUES.ID.eq(id))
            .fetchOne()
            ?.let { mapLeague(it) }

    fun findAll(): List<League> =
        dsl.selectFrom(LEAGUES)
            .orderBy(LEAGUES.NAME)
            .fetch()
            .map { mapLeague(it) }

    fun insert(league: League): Int =
        dsl.insertInto(LEAGUES)
            .set(LEAGUES.NAME, league.name)
            .set(LEAGUES.COUNTRY, league.country)
            .set(LEAGUES.SEASON, league.season)
            .set(LEAGUES.API_ID, league.apiId)
            .returningResult(LEAGUES.ID)
            .fetchOne()!!
            .getValue(LEAGUES.ID)

    private fun mapLeague(r: Record): League = League(
        id = r[LEAGUES.ID],
        name = r[LEAGUES.NAME],
        country = r[LEAGUES.COUNTRY],
        season = r[LEAGUES.SEASON],
        apiId = r[LEAGUES.API_ID],
        createdAt = r[LEAGUES.CREATED_AT],
    )
}
```

## Rules
- Use generated JOOQ table objects (`LEAGUES`, `TEAMS`, etc.)
- Map records to domain models in private `map*` functions
- `findXxx` returns nullable, `getXxx` throws
- Inject `DSLContext` via constructor (autoBind resolves it)
- One DAO per database table
