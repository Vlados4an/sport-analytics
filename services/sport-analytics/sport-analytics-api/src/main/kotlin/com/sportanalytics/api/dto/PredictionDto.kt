package com.sportanalytics.api.dto

data class PredictionResponse(
    val homeWinProb: String,
    val drawProb: String,
    val awayWinProb: String,
    val predictedScore: String,
    val lambdaHome: String,
    val muAway: String,
    val model: String,
)

data class RatingResponse(
    val teamId: Int,
    val rating: Double,
    val attackRating: Double?,
    val defenseRating: Double?,
    val form: Double?,
)
