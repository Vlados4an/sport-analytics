import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ChevronRight } from 'lucide-react'
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts'
import { fetchMatches, fetchRatings, fetchLeagues } from '../api/client'
import type { Match, TeamRating } from '../types'

export default function DashboardPage() {
  const [matches, setMatches] = useState<Match[]>([])
  const [ratings, setRatings] = useState<TeamRating[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([fetchLeagues(), fetchMatches(1), fetchRatings(1)])
      .then(([_l, m, r]) => { setMatches(m); setRatings(r) })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const finished = matches.filter(m => m.status === 'FINISHED')
  const topTeams = [...ratings].sort((a, b) => b.rating - a.rating).slice(0, 5)

  const homeWins = finished.filter(m => m.homeScore != null && m.awayScore != null && m.homeScore > m.awayScore).length
  const draws = finished.filter(m => m.homeScore != null && m.awayScore != null && m.homeScore === m.awayScore).length
  const awayWins = finished.filter(m => m.homeScore != null && m.awayScore != null && m.homeScore < m.awayScore).length
  const pieData = [
    { name: 'Хозяева', value: homeWins, color: '#7048e8' },
    { name: 'Ничья', value: draws, color: '#fbbf24' },
    { name: 'Гости', value: awayWins, color: '#9171f8' },
  ]

  if (loading) return <div className="flex justify-center py-20"><div className="w-6 h-6 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" /></div>

  return (
    <div className="space-y-8">
      {/* Hero */}
      <div>
        <h1 className="text-[28px] font-bold text-ink-900 tracking-tight">Premier League</h1>
        <p className="text-ink-400 text-sm mt-0.5">Сезон 2024/25 &middot; {matches.length} матчей &middot; {ratings.length} команд</p>
      </div>

      {/* Stats strip */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {[
          { n: finished.length, label: 'Матчей сыграно', sub: `из ${matches.length}` },
          { n: ratings.length, label: 'Команд', sub: 'Premier League' },
          { n: topTeams[0] ? Number(topTeams[0].rating).toFixed(1) : '—', label: 'Лучший рейтинг', sub: topTeams[0]?.teamName ?? '' },
          { n: finished.length ? (finished.reduce((s, m) => s + (m.homeScore ?? 0) + (m.awayScore ?? 0), 0) / finished.length).toFixed(1) : '—', label: 'Голов / матч', sub: 'В среднем' },
        ].map((s, i) => (
          <div key={i} className="bg-white rounded-2xl p-4 shadow-card border border-ink-100/60">
            <div className="text-2xl font-bold text-ink-900 font-mono">{s.n}</div>
            <div className="text-[13px] font-medium text-ink-600 mt-1">{s.label}</div>
            <div className="text-[11px] text-ink-400 mt-0.5">{s.sub}</div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        {/* Latest matches */}
        <div className="lg:col-span-3">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-[15px] font-semibold text-ink-800">Последние результаты</h2>
            <Link to="/matches" className="text-[13px] text-brand-600 hover:text-brand-700 font-medium flex items-center gap-0.5">
              Все <ChevronRight className="w-3.5 h-3.5" />
            </Link>
          </div>
          <div className="bg-white rounded-2xl shadow-card border border-ink-100/60 divide-y divide-ink-100/60">
            {finished.slice(-8).reverse().map(m => {
              const hw = (m.homeScore ?? 0) > (m.awayScore ?? 0)
              const aw = (m.awayScore ?? 0) > (m.homeScore ?? 0)
              return (
                <Link key={m.id} to={`/matches/${m.id}`} className="flex items-center px-4 py-3 hover:bg-ink-50/50 transition-colors group">
                  <span className="w-7 text-[11px] text-ink-400 font-mono">{m.round || '—'}</span>
                  <span className={`flex-1 text-[13px] text-right pr-3 truncate ${hw ? 'font-semibold text-ink-900' : 'text-ink-600'}`}>
                    {m.homeTeamName}
                  </span>
                  <span className="font-mono text-[15px] font-bold text-ink-900 w-12 text-center tabular-nums">
                    {m.homeScore}&thinsp;:&thinsp;{m.awayScore}
                  </span>
                  <span className={`flex-1 text-[13px] pl-3 truncate ${aw ? 'font-semibold text-ink-900' : 'text-ink-600'}`}>
                    {m.awayTeamName}
                  </span>
                  <ChevronRight className="w-4 h-4 text-ink-300 group-hover:text-brand-500 ml-2 transition-colors" />
                </Link>
              )
            })}
          </div>
        </div>

        {/* Sidebar */}
        <div className="lg:col-span-2 space-y-6">
          {/* Pie chart */}
          <div className="bg-white rounded-2xl shadow-card border border-ink-100/60 p-5">
            <h2 className="text-[13px] font-semibold text-ink-700 mb-3">Исходы матчей</h2>
            <ResponsiveContainer width="100%" height={160}>
              <PieChart>
                <Pie data={pieData} dataKey="value" cx="50%" cy="50%" innerRadius={42} outerRadius={65} strokeWidth={2} stroke="#f5f5f8">
                  {pieData.map((d, i) => <Cell key={i} fill={d.color} />)}
                </Pie>
                <Tooltip
                  contentStyle={{ background: '#fff', border: '1px solid #ebebf0', borderRadius: '10px', fontSize: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.06)' }}
                />
              </PieChart>
            </ResponsiveContainer>
            <div className="flex justify-center gap-4 mt-2">
              {pieData.map(d => (
                <div key={d.name} className="flex items-center gap-1.5 text-[11px] text-ink-500">
                  <div className="w-2 h-2 rounded-full" style={{ background: d.color }} />
                  {d.name} <span className="font-mono font-medium text-ink-700">{d.value}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Top 5 */}
          <div className="bg-white rounded-2xl shadow-card border border-ink-100/60 p-5">
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-[13px] font-semibold text-ink-700">Топ рейтинг</h2>
              <Link to="/ratings" className="text-[11px] text-brand-600 font-medium">Все &rarr;</Link>
            </div>
            <div className="space-y-2">
              {topTeams.map((t, i) => (
                <div key={t.teamId} className="flex items-center gap-3 py-1.5">
                  <span className={`w-5 text-[13px] font-bold font-mono ${i === 0 ? 'text-amber-500' : i === 1 ? 'text-ink-400' : i === 2 ? 'text-amber-700' : 'text-ink-300'}`}>
                    {i + 1}
                  </span>
                  <span className="flex-1 text-[13px] font-medium text-ink-800 truncate">{t.teamName}</span>
                  <span className="font-mono text-[13px] font-bold text-brand-600">{Number(t.rating).toFixed(1)}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
