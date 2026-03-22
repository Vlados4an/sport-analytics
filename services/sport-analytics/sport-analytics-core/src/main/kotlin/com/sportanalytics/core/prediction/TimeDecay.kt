package com.sportanalytics.core.prediction

import kotlin.math.exp
import kotlin.math.ln

object TimeDecay {
    const val DEFAULT_XI: Double = 0.0019

    fun weight(daysAgo: Long, xi: Double = DEFAULT_XI): Double {
        require(daysAgo >= 0) { "daysAgo must be non-negative" }
        return exp(-xi * daysAgo)
    }

    fun halfLife(xi: Double = DEFAULT_XI): Double = ln(2.0) / xi

    fun weights(daysAgoList: List<Long>, xi: Double = DEFAULT_XI): List<Double> =
        daysAgoList.map { weight(it, xi) }
}
