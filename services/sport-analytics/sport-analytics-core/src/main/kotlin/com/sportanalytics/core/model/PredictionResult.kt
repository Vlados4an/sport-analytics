package com.sportanalytics.core.model

data class PredictionResult(
    val homeWinProb: Double,
    val drawProb: Double,
    val awayWinProb: Double,
    val predictedHomeScore: Int,
    val predictedAwayScore: Int,
    val scoreMatrix: Array<DoubleArray>,
    val lambdaHome: Double,
    val muAway: Double,
    val modelVersion: String = "dixon-coles-xg-v1",
)
