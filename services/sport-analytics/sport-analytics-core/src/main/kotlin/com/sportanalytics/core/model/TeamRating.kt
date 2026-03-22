package com.sportanalytics.core.model

import java.math.BigDecimal
import java.time.Instant

data class TeamRating(
    val id: Int = 0,
    val teamId: Int,
    val leagueId: Int,
    val rating: BigDecimal,
    val attackRating: BigDecimal? = null,
    val defenseRating: BigDecimal? = null,
    val form: BigDecimal? = null,
    val calculatedAt: Instant = Instant.now(),
)
