package com.sportanalytics.core.etl

import com.sportanalytics.core.model.Match
import com.sportanalytics.core.model.MatchStats

/**
 * Pre-persist validation of external data (diploma 3.3, Лист 2):
 * required fields, numeric ranges and referential integrity checks.
 * Records failing validation are rejected and logged to event_log.
 */
class ValidationService {

    data class ValidationResult(val errors: List<String>) {
        val isValid: Boolean get() = errors.isEmpty()
    }

    fun validateMatch(match: Match, knownTeamIds: Set<Int>): ValidationResult {
        val errors = mutableListOf<String>()

        if (match.homeTeamId == match.awayTeamId) {
            errors += "home and away team must differ (match ${match.id})"
        }
        if (match.homeTeamId !in knownTeamIds) {
            errors += "unknown home_team_id ${match.homeTeamId}"
        }
        if (match.awayTeamId !in knownTeamIds) {
            errors += "unknown away_team_id ${match.awayTeamId}"
        }
        match.homeScore?.let { if (it < 0 || it > 15) errors += "home_score out of range: $it" }
        match.awayScore?.let { if (it < 0 || it > 15) errors += "away_score out of range: $it" }

        return ValidationResult(errors)
    }

    fun validateStats(stats: MatchStats): ValidationResult {
        val errors = mutableListOf<String>()

        stats.possession?.let {
            if (it.toDouble() < 0 || it.toDouble() > 100) errors += "possession out of range: $it"
        }
        val shots = stats.shots ?: 0
        val onTarget = stats.shotsOnTarget ?: 0

        if (shots < 0) errors += "shots negative: $shots"
        if (onTarget > shots) errors += "shots_on_target ($onTarget) exceeds shots ($shots)"
        stats.passAccuracy?.let {
            if (it.toDouble() < 0 || it.toDouble() > 100) errors += "pass_accuracy out of range: $it"
        }

        return ValidationResult(errors)
    }
}
