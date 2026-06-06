import { useEffect, useState } from 'react'
import { RadarChart, Radar, PolarGrid, PolarAngleAxis, ResponsiveContainer } from 'recharts'
import clsx from 'clsx'
import { fetchRatings } from '../api/client'
import type { TeamRating } from '../types'

export default function RatingsPage() {
  const [ratings, setRatings] = useState<TeamRating[]>([])
  const [selected, setSelected] = useState<TeamRating | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchRatings(1)
      .then(data => {
        const sorted = [...data].sort((a: TeamRating, b: TeamRating) => b.rating - a.rating)
        setRatings(sorted)
        if (sorted.length > 0) setSelected(sorted[0])
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="flex justify-center py-20"><div className="w-6 h-6 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" /></div>

  const radarData = selected
    ? [
        { m: 'Атака', v: Number(selected.attackRating) },
        { m: 'Оборона', v: Number(selected.defenseRating) },
        { m: 'Форма', v: Number(selected.form) },
        { m: 'Рейтинг', v: Number(selected.rating) },
      ]
    : []

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-[28px] font-bold text-ink-900 tracking-tight">Рейтинги</h1>
        <p className="text-ink-400 text-sm">R = 0.40 &times; I<sub>a</sub> + 0.35 &times; I<sub>d</sub> + 0.25 &times; F</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Table */}
        <div className="lg:col-span-2 bg-white rounded-2xl shadow-card border border-ink-100/60 overflow-hidden">
          <table className="w-full text-[13px]">
            <thead>
              <tr className="border-b border-ink-100 bg-ink-50/50">
                <th className="text-left font-semibold text-ink-500 px-4 py-2.5 w-10">#</th>
                <th className="text-left font-semibold text-ink-500 px-3 py-2.5">Команда</th>
                <th className="text-center font-semibold text-ink-500 px-3 py-2.5 w-16">ATK</th>
                <th className="text-center font-semibold text-ink-500 px-3 py-2.5 w-16">DEF</th>
                <th className="text-center font-semibold text-ink-500 px-3 py-2.5 w-16">FRM</th>
                <th className="text-center font-semibold text-ink-500 px-3 py-2.5 w-20">Rating</th>
              </tr>
            </thead>
            <tbody>
              {ratings.map((t, i) => (
                <tr
                  key={t.teamId}
                  onClick={() => setSelected(t)}
                  className={clsx(
                    'border-b border-ink-100/50 cursor-pointer transition-colors',
                    selected?.teamId === t.teamId ? 'bg-brand-50' : 'hover:bg-ink-50/60',
                  )}
                >
                  <td className="px-4 py-3">
                    <span className={clsx('font-bold font-mono text-[13px]', i === 0 ? 'text-amber-500' : i === 1 ? 'text-ink-400' : i === 2 ? 'text-amber-700' : 'text-ink-300')}>
                      {i + 1}
                    </span>
                  </td>
                  <td className="px-3 py-3 font-medium text-ink-800">{t.teamName}</td>
                  <td className="px-3 py-3 text-center font-mono font-semibold text-emerald-600">{Number(t.attackRating).toFixed(1)}</td>
                  <td className="px-3 py-3 text-center font-mono font-semibold text-sky-600">{Number(t.defenseRating).toFixed(1)}</td>
                  <td className="px-3 py-3 text-center font-mono font-semibold text-amber-600">{Number(t.form).toFixed(1)}</td>
                  <td className="px-3 py-3 text-center">
                    <span className="font-mono text-[15px] font-bold text-brand-600">{Number(t.rating).toFixed(1)}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Sidebar card */}
        {selected && (
          <div className="space-y-4">
            <div className="bg-white rounded-2xl shadow-card border border-ink-100/60 p-5">
              <div className="flex items-center justify-between mb-1">
                <h3 className="text-[15px] font-semibold text-ink-800">{selected.teamName}</h3>
                <span className="font-mono text-[22px] font-black text-brand-600">{Number(selected.rating).toFixed(1)}</span>
              </div>

              <ResponsiveContainer width="100%" height={200}>
                <RadarChart data={radarData} cx="50%" cy="50%" outerRadius="72%">
                  <PolarGrid stroke="#ebebf0" />
                  <PolarAngleAxis dataKey="m" tick={{ fill: '#6b6b8a', fontSize: 11 }} />
                  <Radar dataKey="v" stroke="#7048e8" fill="#7048e8" fillOpacity={0.15} strokeWidth={2} dot={{ r: 3, fill: '#7048e8' }} />
                </RadarChart>
              </ResponsiveContainer>
            </div>

            <div className="bg-white rounded-2xl shadow-card border border-ink-100/60 p-5 space-y-3">
              {[
                { label: 'Индекс атаки', v: Number(selected.attackRating), color: 'bg-emerald-500' },
                { label: 'Индекс обороны', v: Number(selected.defenseRating), color: 'bg-sky-500' },
                { label: 'Текущая форма', v: Number(selected.form), color: 'bg-amber-500' },
              ].map(r => (
                <div key={r.label}>
                  <div className="flex justify-between text-[12px] mb-1">
                    <span className="text-ink-500">{r.label}</span>
                    <span className="font-mono font-semibold text-ink-700">{r.v.toFixed(1)}</span>
                  </div>
                  <div className="h-1.5 bg-ink-100 rounded-full overflow-hidden">
                    <div className={clsx('h-full rounded-full', r.color)} style={{ width: `${r.v}%` }} />
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
