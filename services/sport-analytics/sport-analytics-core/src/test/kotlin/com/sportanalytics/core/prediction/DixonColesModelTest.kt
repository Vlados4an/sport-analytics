package com.sportanalytics.core.prediction

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class DixonColesModelTest : FunSpec({

    val model = DixonColesModel()

    test("Poisson P(0) with lambda=1.5 ≈ 0.2231") {
        PoissonDistribution.pmf(0, 1.5) shouldBe (0.2231 plusOrMinus 0.001)
    }

    test("time decay: 365 days ago ≈ 0.5") {
        TimeDecay.weight(365) shouldBe (0.5 plusOrMinus 0.02)
    }

    test("corrected matrix sums to 1.0") {
        val m = ScoreMatrix.buildCorrectedMatrix(1.5, 1.2)
        m.sumOf { it.sum() } shouldBe (1.0 plusOrMinus 0.001)
    }

    test("Dixon-Coles correction increases P(0:0) for negative rho") {
        val raw = ScoreMatrix.buildRawMatrix(1.5, 1.2)
        val rawP00 = raw[0][0]
        val corrected = ScoreMatrix.buildRawMatrix(1.5, 1.2)
        ScoreMatrix.applyDixonColesCorrection(corrected, 1.5, 1.2, -0.07)
        assert(corrected[0][0] > rawP00)
    }

    test("diploma example: Arsenal 1.76 vs Man City 0.95") {
        val result = model.predictFromLambdaMu(1.76, 0.95)
        result.homeWinProb shouldBe (0.55 plusOrMinus 0.03)
        result.drawProb shouldBe (0.25 plusOrMinus 0.03)
        result.awayWinProb shouldBe (0.20 plusOrMinus 0.03)
        val total = result.homeWinProb + result.drawProb + result.awayWinProb
        total shouldBe (1.0 plusOrMinus 0.001)
    }

    test("full pipeline with team data") {
        val homeMatches = listOf(
            AttackDefenseStrength.TeamMatchData(2.1, 0.8, 7),
            AttackDefenseStrength.TeamMatchData(1.5, 1.2, 21),
            AttackDefenseStrength.TeamMatchData(1.8, 0.9, 35),
        )
        val awayMatches = listOf(
            AttackDefenseStrength.TeamMatchData(1.4, 1.3, 10),
            AttackDefenseStrength.TeamMatchData(1.7, 0.9, 24),
            AttackDefenseStrength.TeamMatchData(1.2, 1.1, 38),
        )
        val leagueAvg = AttackDefenseStrength.LeagueAverages(1.35, 1.35, 1.52, 1.18)

        val result = model.predict(
            DixonColesModel.TeamData(homeMatches, 1.05),
            DixonColesModel.TeamData(awayMatches, 0.98),
            leagueAvg,
        )

        assert(result.homeWinProb > result.awayWinProb)
        val total = result.homeWinProb + result.drawProb + result.awayWinProb
        total shouldBe (1.0 plusOrMinus 0.001)
    }

    test("equal teams produce balanced probabilities") {
        val matches = listOf(AttackDefenseStrength.TeamMatchData(1.35, 1.35, 7))
        val avg = AttackDefenseStrength.LeagueAverages(1.35, 1.35, 1.35, 1.35)
        val result = model.predict(
            DixonColesModel.TeamData(matches), DixonColesModel.TeamData(matches), avg,
        )
        val diff = kotlin.math.abs(result.homeWinProb - result.awayWinProb)
        assert(diff < 0.05) { "Expected balanced: hw=${result.homeWinProb} aw=${result.awayWinProb}" }
    }
})
