package com.sportanalytics.core.seed

/**
 * Standard round-robin fixture generator (circle method).
 * For n teams: rounds 1..(n-1) are the first half of the season,
 * rounds n..2(n-1) mirror the first half with home/away swapped.
 */
object FixtureGenerator {

    data class Fixture(val homeIdx: Int, val awayIdx: Int)

    fun roundFixtures(teamCount: Int, round: Int): List<Fixture> {
        val half = teamCount - 1
        val baseRound = if (round <= half) round else round - half
        val mirrored = round > half

        val teams = (0 until teamCount).toMutableList()
        repeat(baseRound - 1) {
            val last = teams.removeAt(teams.lastIndex)
            teams.add(1, last)
        }

        return (0 until teamCount / 2).map { m ->
            val a = teams[m]
            val b = teams[teamCount - 1 - m]
            // Alternate home/away by round parity so teams don't always host
            val (home, away) = if ((baseRound + m) % 2 == 0) a to b else b to a

            if (mirrored) Fixture(away, home) else Fixture(home, away)
        }
    }
}
