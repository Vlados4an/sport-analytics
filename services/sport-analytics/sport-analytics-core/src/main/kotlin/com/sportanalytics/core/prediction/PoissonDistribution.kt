package com.sportanalytics.core.prediction

import kotlin.math.exp
import kotlin.math.pow

object PoissonDistribution {

    fun pmf(k: Int, lambda: Double): Double {
        require(k >= 0) { "k must be non-negative, got $k" }
        require(lambda > 0) { "lambda must be positive, got $lambda" }
        return lambda.pow(k) * exp(-lambda) / factorial(k)
    }

    fun pmfArray(lambda: Double, maxGoals: Int = 6): DoubleArray =
        DoubleArray(maxGoals + 1) { k -> pmf(k, lambda) }

    fun cdf(k: Int, lambda: Double): Double =
        (0..k).sumOf { pmf(it, lambda) }

    private fun factorial(n: Int): Double {
        if (n <= 1) return 1.0
        var result = 1.0
        for (i in 2..n) result *= i
        return result
    }
}
