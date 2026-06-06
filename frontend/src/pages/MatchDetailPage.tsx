import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, MapPin, Users } from 'lucide-react'
import clsx from 'clsx'
import { fetchMatch, fetchMatchStats } from '../api/client'
import type { Match, MatchStats } from '../types'

export default function MatchDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [match, setMatch] = useState<Match | null>(null)
  const [stats, setStats] = useState<MatchStats[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    const mid = Number(id)
    Promise.all([fetchMatch(mid), fetchMatchStats(mid)])
      .then(([m, s]) => { setMatch(m); setStats(s) })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <div className="flex justify-center py-20"><div className="w-6 h-6 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" /></div>
  if (!match) return <div className="text-ink-400 py-12 text-center">Матч не найден</div>

  const home = stats.find(s => s.teamId === match.homeTeamId)
  const away = stats.find(s => s.teamId === match.awayTeamId)
  const fin = match.status === 'FINISHED'

  const rows = [
    { label: 'Владение, %', h: home?.possession ?? 0, a: away?.possession ?? 0 },
    { label: 'Удары', h: home?.shots ?? 0, a: away?.shots ?? 0 },
    { label: 'В створ', h: home?.shotsOnTarget ?? 0, a: away?.shotsOnTarget ?? 0 },
    { label: 'Угловые', h: home?.corners ?? 0, a: away?.corners ?? 0 },
    { label: 'Фолы', h: home?.fouls ?? 0, a: away?.fouls ?? 0 },
    { label: 'Карточки', h: home?.yellowCards ?? 0, a: away?.yellowCards ?? 0 },
    { label: 'Офсайды', h: home?.offsides ?? 0, a: away?.offsides ?? 0 },
    { label: 'Передачи', h: home?.passes ?? 0, a: away?.passes ?? 0 },
  ]

  return (
    <div className="max-w-2xl mx-auto space-y-5">
      <Link to="/matches" className="inline-flex items-center gap-1.5 text-[13px] text-ink-400 hover:text-brand-600 transition-colors">
        <ArrowLeft className="w-4 h-4" /> Все матчи
      </Link>

      {/* Score card */}
      <div className="bg-white rounded-2xl shadow-card border border-ink-100/60 overflow-hidden">
        <div className="bg-gradient-to-r from-brand-600 to-brand-500 px-6 py-8 text-center text-white">
          <div className="text-[11px] font-medium opacity-75 mb-5">{match.round ? `Тур ${match.round}` : 'Premier League'}</div>
          <div className="flex items-center justify-center gap-6">
            <div className="flex-1 text-right">
              <div className="text-[17px] font-semibold">{match.homeTeamName}</div>
            </div>
            {fin ? (
              <div className="flex items-baseline gap-2 font-mono">
                <span className="text-[42px] font-black leading-none">{match.homeScore}</span>
                <span className="text-[24px] opacity-50">:</span>
                <span className="text-[42px] font-black leading-none">{match.awayScore}</span>
              </div>
            ) : (
              <div className="text-[18px] font-semibold opacity-60">vs</div>
            )}
            <div className="flex-1 text-left">
              <div className="text-[17px] font-semibold">{match.awayTeamName}</div>
            </div>
          </div>
          <div className="flex items-center justify-center gap-4 mt-4 text-[11px] opacity-70">
            {match.stadium && <span className="flex items-center gap-1"><MapPin className="w-3 h-3" /> {match.stadium}</span>}
            {match.attendance && <span className="flex items-center gap-1"><Users className="w-3 h-3" /> {match.attendance.toLocaleString()}</span>}
          </div>
        </div>

        {/* Stats */}
        {stats.length > 0 && (
          <div className="px-6 py-5 space-y-4">
            <h3 className="text-[13px] font-semibold text-ink-700">Статистика матча</h3>
            {rows.map(r => {
              const total = r.h + r.a || 1
              const hp = (r.h / total) * 100
              const ap = (r.a / total) * 100
              const hWins = r.h > r.a
              const aWins = r.a > r.h
              return (
                <div key={r.label} className="space-y-1">
                  <div className="flex items-center justify-between text-[13px]">
                    <span className={clsx('font-mono font-semibold w-8', hWins ? 'text-brand-600' : 'text-ink-700')}>{r.h}</span>
                    <span className="text-ink-400 text-[12px]">{r.label}</span>
                    <span className={clsx('font-mono font-semibold w-8 text-right', aWins ? 'text-brand-600' : 'text-ink-700')}>{r.a}</span>
                  </div>
                  <div className="flex gap-0.5 h-[5px]">
                    <div className={clsx('rounded-l-full transition-all', hWins ? 'bg-brand-500' : 'bg-ink-200')} style={{ width: `${hp}%` }} />
                    <div className={clsx('rounded-r-full transition-all', aWins ? 'bg-brand-500' : 'bg-ink-200')} style={{ width: `${ap}%` }} />
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>

      <div className="text-center">
        <Link
          to={`/predictions?home=${match.homeTeamId}&away=${match.awayTeamId}`}
          className="inline-flex items-center gap-2 px-5 py-2.5 bg-brand-600 hover:bg-brand-700 text-white text-[13px] font-semibold rounded-xl transition-colors shadow-sm"
        >
          Рассчитать прогноз
        </Link>
      </div>
    </div>
  )
}
