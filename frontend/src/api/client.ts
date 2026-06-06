import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// --- Auth session (JWT) ---

export interface AuthUser {
  login: string
  role: string
}

export const getAuthUser = (): AuthUser | null => {
  const login = localStorage.getItem('auth.login')
  const role = localStorage.getItem('auth.role')
  return login ? { login, role: role || 'user' } : null
}

export const isAuthenticated = () => !!localStorage.getItem('auth.accessToken')

export const logout = () => {
  localStorage.removeItem('auth.accessToken')
  localStorage.removeItem('auth.refreshToken')
  localStorage.removeItem('auth.login')
  localStorage.removeItem('auth.role')
}

const saveSession = (data: any) => {
  if (data?.accessToken) localStorage.setItem('auth.accessToken', data.accessToken)
  if (data?.refreshToken) localStorage.setItem('auth.refreshToken', data.refreshToken)
  if (data?.login) localStorage.setItem('auth.login', data.login)
  if (data?.role) localStorage.setItem('auth.role', data.role)
}

api.interceptors.request.use(config => {
  const token = localStorage.getItem('auth.accessToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
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
  api.post('/auth/login', { login: loginName, password }).then(r => {
    saveSession(r.data)
    return r.data
  })
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
