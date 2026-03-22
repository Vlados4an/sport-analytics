package com.sportanalytics.core.model

import java.math.BigDecimal
import java.time.Instant

data class Prediction(
    val id: Int = 0,
    val matchId: Int,
    val homeWinProb: BigDecimal,
    val drawProb: BigDecimal,
    val awayWinProb: BigDecimal,
    val predictedHomeScore: Int,
    val predictedAwayScore: Int,
    val modelVersion: String = "dixon-coles-xg-v1",
    val createdAt: Instant = Instant.now(),
)
