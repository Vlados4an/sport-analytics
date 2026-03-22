package com.sportanalytics.core.prediction

object ScoreMatrix {
    const val MAX_GOALS: Int = 6

    data class OutcomeProbabilities(val homeWin: Double, val draw: Double, val awayWin: Double)
    data class MostProbableScore(val homeGoals: Int, val awayGoals: Int, val probability: Double)

    fun buildRawMatrix(lambda: Double, mu: Double, maxGoals: Int = MAX_GOALS): Array<DoubleArray> {
        val homePmf = PoissonDistribution.pmfArray(lambda, maxGoals)
        val awayPmf = PoissonDistribution.pmfArray(mu, maxGoals)
        return Array(maxGoals + 1) { x -> DoubleArray(maxGoals + 1) { y -> homePmf[x] * awayPmf[y] } }
    }

    fun applyDixonColesCorrection(matrix: Array<DoubleArray>, lambda: Double, mu: Double, rho: Double) {
        if (matrix.size < 2 || matrix[0].size < 2) return
        matrix[0][0] *= (1 - lambda * mu * rho)
        matrix[0][1] *= (1 + lambda * rho)
        matrix[1][0] *= (1 + mu * rho)
        matrix[1][1] *= (1 - rho)
    }

    fun normalize(matrix: Array<DoubleArray>) {
        val total = matrix.sumOf { it.sum() }
        if (total <= 0) return
        for (row in matrix) for (i in row.indices) row[i] /= total
    }

    fun outcomeProbabilities(matrix: Array<DoubleArray>): OutcomeProbabilities {
        var hw = 0.0; var d = 0.0; var aw = 0.0
        for (x in matrix.indices) for (y in matrix[x].indices) when {
            x > y -> hw += matrix[x][y]
            x == y -> d += matrix[x][y]
            else -> aw += matrix[x][y]
        }
        return OutcomeProbabilities(hw, d, aw)
    }

    fun mostProbableScore(matrix: Array<DoubleArray>): MostProbableScore {
        var bx = 0; var by = 0; var bp = 0.0
        for (x in matrix.indices) for (y in matrix[x].indices) if (matrix[x][y] > bp) { bp = matrix[x][y]; bx = x; by = y }
        return MostProbableScore(bx, by, bp)
    }

    fun buildCorrectedMatrix(lambda: Double, mu: Double, rho: Double = -0.07, maxGoals: Int = MAX_GOALS): Array<DoubleArray> {
        val m = buildRawMatrix(lambda, mu, maxGoals)
        applyDixonColesCorrection(m, lambda, mu, rho)
        normalize(m)
        return m
    }
}
