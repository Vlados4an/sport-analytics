import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

export const fetchLeagues = () => api.get('/leagues').then(r => r.data)
export const fetchLeagueTeams = (id: number) => api.get(`/leagues/${id}/teams`).then(r => r.data)
export const fetchTeam = (id: number) => api.get(`/teams/${id}`).then(r => r.data)
export const fetchTeamRating = (id: number) => api.get(`/teams/${id}/rating`).then(r => r.data)
export const fetchMatches = (leagueId: number) => api.get(`/matches?league_id=${leagueId}`).then(r => r.data)
export const fetchMatch = (id: number) => api.get(`/matches/${id}`).then(r => r.data)
export const fetchMatchStats = (id: number) => api.get(`/matches/${id}/stats`).then(r => r.data)
export const fetchRatings = (leagueId: number) => api.get(`/ratings?league_id=${leagueId}`).then(r => r.data)
export const calculatePrediction = (homeTeamId: number, awayTeamId: number) =>
  api.post('/predictions/calculate', { homeTeamId, awayTeamId }).then(r => r.data)
export const fetchPrediction = (matchId: number) => api.get(`/predictions/${matchId}`).then(r => r.data)
export const login = (loginName: string, password: string) =>
  api.post('/auth/login', { login: loginName, password }).then(r => r.data)
export const register = (loginName: string, password: string) =>
  api.post('/auth/register', { login: loginName, password }).then(r => r.data)
export const seedData = () => api.post('/admin/seed').then(r => r.data)
export const recalculateRatings = () => api.post('/admin/recalculate-ratings', {}).then(r => r.data)
export const runEtl = () => api.post('/admin/etl/run').then(r => r.data)
export const fetchAdminEvents = (limit = 30) => api.get(`/admin/events?limit=${limit}`).then(r => r.data)
export const fetchAdminStatus = () => api.get('/admin/status').then(r => r.data)
export const clearCache = () => api.post('/admin/cache/clear').then(r => r.data)
export const fetchStandings = (leagueId: number) => api.get(`/leagues/${leagueId}/standings`).then(r => r.data)

export default api
