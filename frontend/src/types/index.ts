export interface League {
  id: number
  name: string
  country: string
  season: string
}

export interface Team {
  id: number
  name: string
  leagueId: number
  shortName?: string
  logoUrl?: string
}

export interface Match {
  id: number
  leagueId: number
  homeTeamId: number
  awayTeamId: number
  homeTeamName?: string
  awayTeamName?: string
  homeScore: number | null
  awayScore: number | null
  matchDate: string
  round: number | null
  status: string
  stadium?: string
  referee?: string
  attendance?: number
}

export interface MatchStats {
  id: number
  matchId: number
  teamId: number
  teamName?: string
  possession?: number
  shots?: number
  shotsOnTarget?: number
  corners?: number
  fouls?: number
  yellowCards?: number
  redCards?: number
  offsides?: number
  passes?: number
  passAccuracy?: number
}

export interface TeamRating {
  id?: number
  teamId: number
  teamName?: string
  leagueId: number
  rating: number
  attackRating: number
  defenseRating: number
  form: number
  createdAt?: string
}

export interface Prediction {
  homeWinProb: string
  drawProb: string
  awayWinProb: string
  predictedScore: string
  lambdaHome: string
  muAway: string
  model: string
  scoreMatrix?: number[][]
}

export interface PredictionStored {
  id: number
  matchId: number
  homeWinProb: number
  drawProb: number
  awayWinProb: number
  predictedHomeScore: number
  predictedAwayScore: number
}

export interface User {
  login: string
  role: string
}
