package com.sportanalytics.core.model

import java.math.BigDecimal

data class MatchStats(
    val id: Int = 0,
    val matchId: Int,
    val teamId: Int,
    val possession: BigDecimal? = null,
    val shots: Int? = null,
    val shotsOnTarget: Int? = null,
    val corners: Int? = null,
    val fouls: Int? = null,
    val yellowCards: Int? = null,
    val redCards: Int? = null,
    val offsides: Int? = null,
    val passes: Int? = null,
    val passAccuracy: BigDecimal? = null,
)
