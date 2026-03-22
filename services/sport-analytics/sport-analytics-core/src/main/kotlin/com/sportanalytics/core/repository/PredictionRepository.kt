package com.sportanalytics.core.repository

import com.sportanalytics.core.model.Prediction
import com.sportanalytics.jooq.tables.Predictions.PREDICTIONS
import org.jooq.DSLContext
import org.jooq.Record

class PredictionRepository(private val dsl: DSLContext) {

    fun findByMatchId(matchId: Int): Prediction? =
        dsl.selectFrom(PREDICTIONS)
            .where(PREDICTIONS.MATCH_ID.eq(matchId))
            .orderBy(PREDICTIONS.CREATED_AT.desc())
            .limit(1)
            .fetchOne()
            ?.let { mapPrediction(it) }

    fun insert(prediction: Prediction): Int =
        dsl.insertInto(PREDICTIONS)
            .set(PREDICTIONS.MATCH_ID, prediction.matchId)
            .set(PREDICTIONS.HOME_WIN_PROB, prediction.homeWinProb)
            .set(PREDICTIONS.DRAW_PROB, prediction.drawProb)
            .set(PREDICTIONS.AWAY_WIN_PROB, prediction.awayWinProb)
            .set(PREDICTIONS.PREDICTED_HOME_SCORE, prediction.predictedHomeScore)
            .set(PREDICTIONS.PREDICTED_AWAY_SCORE, prediction.predictedAwayScore)
            .set(PREDICTIONS.MODEL_VERSION, prediction.modelVersion)
            .returningResult(PREDICTIONS.ID)
            .fetchOne()!!
            .getValue(PREDICTIONS.ID)

    private fun mapPrediction(r: Record) = Prediction(
        id = r[PREDICTIONS.ID], matchId = r[PREDICTIONS.MATCH_ID],
        homeWinProb = r[PREDICTIONS.HOME_WIN_PROB], drawProb = r[PREDICTIONS.DRAW_PROB],
        awayWinProb = r[PREDICTIONS.AWAY_WIN_PROB],
        predictedHomeScore = r[PREDICTIONS.PREDICTED_HOME_SCORE],
        predictedAwayScore = r[PREDICTIONS.PREDICTED_AWAY_SCORE],
        modelVersion = r[PREDICTIONS.MODEL_VERSION], createdAt = r[PREDICTIONS.CREATED_AT],
    )
}
