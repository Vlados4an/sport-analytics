package com.sportanalytics.core.model

data class GoalEvent(
    val id: Int = 0,
    val matchId: Int,
    val teamId: Int,
    val playerId: Int? = null,
    val minute: Int,
    val isPenalty: Boolean = false,
    val isOwnGoal: Boolean = false,
)
