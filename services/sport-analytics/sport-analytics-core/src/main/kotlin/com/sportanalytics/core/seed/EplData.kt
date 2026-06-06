package com.sportanalytics.core.seed

/**
 * Premier League 2024-2025 reference data used by the seed and the simulated ETL source.
 * Attack/defense strengths approximate the real season so generated ratings look plausible.
 */
data class EplTeam(
    val name: String,
    val shortName: String,
    val stadium: String,
    val capacity: Int,
    val attack: Double,
    val defense: Double,
)

object EplData {

    val TEAMS = listOf(
        EplTeam("Liverpool", "LIV", "Anfield", 61_276, 2.1, 0.85),
        EplTeam("Arsenal", "ARS", "Emirates Stadium", 60_704, 1.8, 0.80),
        EplTeam("Manchester City", "MCI", "Etihad Stadium", 52_900, 1.9, 1.05),
        EplTeam("Chelsea", "CHE", "Stamford Bridge", 40_173, 1.7, 1.05),
        EplTeam("Newcastle", "NEW", "St James' Park", 52_305, 1.8, 1.10),
        EplTeam("Aston Villa", "AVL", "Villa Park", 42_918, 1.5, 1.15),
        EplTeam("Nottingham Forest", "NFO", "City Ground", 30_404, 1.5, 0.95),
        EplTeam("Brighton", "BHA", "Amex Stadium", 31_876, 1.6, 1.25),
        EplTeam("Bournemouth", "BOU", "Vitality Stadium", 11_307, 1.5, 1.10),
        EplTeam("Brentford", "BRE", "Gtech Community Stadium", 17_250, 1.7, 1.30),
        EplTeam("Fulham", "FUL", "Craven Cottage", 29_600, 1.4, 1.20),
        EplTeam("Crystal Palace", "CRY", "Selhurst Park", 25_486, 1.3, 1.15),
        EplTeam("Manchester United", "MUN", "Old Trafford", 74_310, 1.2, 1.20),
        EplTeam("Tottenham", "TOT", "Tottenham Hotspur Stadium", 62_850, 1.7, 1.45),
        EplTeam("Everton", "EVE", "Goodison Park", 39_414, 1.1, 1.10),
        EplTeam("West Ham", "WHU", "London Stadium", 62_500, 1.2, 1.50),
        EplTeam("Wolves", "WOL", "Molineux", 31_750, 1.4, 1.60),
        EplTeam("Ipswich Town", "IPS", "Portman Road", 30_056, 1.0, 1.70),
        EplTeam("Leicester City", "LEI", "King Power Stadium", 32_259, 0.9, 1.75),
        EplTeam("Southampton", "SOU", "St Mary's Stadium", 32_384, 0.7, 1.85),
    )

    val REFEREES = listOf(
        "Michael Oliver", "Anthony Taylor", "Paul Tierney", "Simon Hooper",
        "Craig Pawson", "Stuart Attwell", "Robert Jones", "Jarred Gillett",
        "Peter Bankes", "David Coote", "John Brooks", "Andrew Madley",
    )

    const val SEASON = "2024-2025"
    const val LEAGUE_NAME = "Premier League"
    const val COUNTRY = "England"
    const val TOTAL_ROUNDS = 38
}
