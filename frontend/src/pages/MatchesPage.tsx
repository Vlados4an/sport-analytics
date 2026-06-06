import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ChevronRight } from 'lucide-react'
import clsx from 'clsx'
import { fetchMatches } from '../api/client'
import type { Match } from '../types'

type Filter = 'ALL' | 'FINISHED' | 'UPCOMING'

export default function MatchesPage() {
  const [matches, setMatches] = useState<Match[]>([])
  const [filter, setFilter] = useState<Filter>('ALL')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchMatches(1).then(setMatches).catch(() => {}).finally(() => setLoading(false))
  }, [])

  const filtered = matches.filter(m => {
    if (filter === 'FINISHED') return m.status === 'FINISHED'
    if (filter === 'UPCOMING') return m.status !== 'FINISHED'
    return true
  })

  const grouped = filtered.reduce<Record<number, Match[]>>((acc, m) => {
    const r = m.round ?? 0
    ;(acc[r] ??= []).push(m)
    return acc
  }, {})

  if (loading) return <div className="flex justify-center py-20"><div className="w-6 h-6 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" /></div>

  return (
    <div className="space-y-6">
      <div className="flex items-end justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-[28px] font-bold text-ink-900 tracking-tight">Все матчи</h1>
          <p className="text-ink-400 text-sm">Premier League 2024/25</p>
        </div>
        <div className="flex gap-1.5">
          {(['ALL', 'FINISHED', 'UPCOMING'] as Filter[]).map(f => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={clsx('pill', filter === f ? 'pill-active' : 'pill-inactive')}
            >
              {f === 'ALL' ? 'Все' : f === 'FINISHED' ? 'Завершённые' : 'Предстоящие'}
            </button>
          ))}
        </div>
      </div>

      {Object.entries(grouped)
        .sort(([a], [b]) => Number(b) - Number(a))
        .map(([round, rm]) => (
          <div key={round}>
            <div className="flex items-center gap-2 mb-2.5">
              <span className="text-[13px] font-semibold text-ink-600">Тур {round}</span>
              <div className="flex-1 h-px bg-ink-100" />
              <span className="text-[11px] text-ink-400">{rm.length} матчей</span>
            </div>

            <div className="bg-white rounded-2xl shadow-card border border-ink-100/60 divide-y divide-ink-100/60 overflow-hidden">
              {rm.map(m => {
                const fin = m.status === 'FINISHED'
                const hw = fin && (m.homeScore ?? 0) > (m.awayScore ?? 0)
                const aw = fin && (m.awayScore ?? 0) > (m.homeScore ?? 0)
                return (
                  <Link key={m.id} to={`/matches/${m.id}`} className="flex items-center px-4 py-3.5 hover:bg-ink-50/60 transition-colors group">
                    <span className={`flex-1 text-[13px] text-right pr-4 truncate ${hw ? 'font-semibold text-ink-900' : 'text-ink-600'}`}>
                      {m.homeTeamName}
                    </span>
                    {fin ? (
                      <div className="flex items-center gap-1 font-mono">
                        <span className={`text-[16px] font-bold w-6 text-right ${hw ? 'text-brand-600' : 'text-ink-800'}`}>{m.homeScore}</span>
                        <span className="text-ink-300 text-[14px]">:</span>
                        <span className={`text-[16px] font-bold w-6 ${aw ? 'text-brand-600' : 'text-ink-800'}`}>{m.awayScore}</span>
                      </div>
                    ) : (
                      <span className="pill pill-inactive text-[10px] !py-0.5 !px-2">Скоро</span>
                    )}
                    <span className={`flex-1 text-[13px] pl-4 truncate ${aw ? 'font-semibold text-ink-900' : 'text-ink-600'}`}>
                      {m.awayTeamName}
                    </span>
                    <ChevronRight className="w-4 h-4 text-ink-300 group-hover:text-brand-500 ml-2 shrink-0 transition-colors" />
                  </Link>
                )
              })}
            </div>
          </div>
        ))}
    </div>
  )
}
