import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Zap } from 'lucide-react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import ScoreMatrix from '../components/ScoreMatrix'
import { fetchLeagueTeams, calculatePrediction } from '../api/client'
import type { Team, Prediction } from '../types'

function poisson(lam: number, maxK: number) {
  return Array.from({ length: maxK }, (_, g) => {
    let p = Math.exp(-lam)
    for (let i = 1; i <= g; i++) p = p * lam / i
    return { goals: `${g}`, prob: +(p * 100).toFixed(1) }
  })
}

export default function PredictionsPage() {
  const [params] = useSearchParams()
  const [teams, setTeams] = useState<Team[]>([])
  const [homeId, setHomeId] = useState<number>(Number(params.get('home')) || 0)
  const [awayId, setAwayId] = useState<number>(Number(params.get('away')) || 0)
  const [result, setResult] = useState<Prediction | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => { fetchLeagueTeams(1).then(setTeams).catch(() => {}) }, [])

  const doPrediction = () => {
    if (!homeId || !awayId || homeId === awayId) return
    setLoading(true)
    calculatePrediction(homeId, awayId).then(setResult).catch(() => {}).finally(() => setLoading(false))
  }

  useEffect(() => {
    if (homeId && awayId && homeId !== awayId && teams.length > 0) doPrediction()
  }, [teams])

  const homeTeam = teams.find(t => t.id === homeId)
  const awayTeam = teams.find(t => t.id === awayId)

  const tooltipStyle = { background: '#fff', border: '1px solid #ebebf0', borderRadius: '10px', fontSize: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.06)' }

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h1 className="text-[28px] font-bold text-ink-900 tracking-tight">Прогноз</h1>
        <p className="text-ink-400 text-sm">Модель Dixon-Coles с коррекцией низких счетов</p>
      </div>

      {/* Selector */}
      <div className="bg-white rounded-2xl shadow-card border border-ink-100/60 p-5">
        <div className="flex flex-col sm:flex-row items-center gap-3">
          <div className="flex-1 w-full">
            <label className="text-[11px] font-semibold text-ink-500 mb-1 block">Хозяева</label>
            <select value={homeId} onChange={e => { setHomeId(Number(e.target.value)); setResult(null) }}
              className="w-full bg-ink-50 border border-ink-200 text-ink-800 rounded-xl px-3 py-2.5 text-[13px] focus:border-brand-400 focus:ring-1 focus:ring-brand-200 focus:outline-none">
              <option value={0}>Выберите команду</option>
              {teams.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
            </select>
          </div>
          <div className="w-10 h-10 rounded-full bg-brand-100 flex items-center justify-center shrink-0">
            <span className="text-brand-600 text-[12px] font-bold">VS</span>
          </div>
          <div className="flex-1 w-full">
            <label className="text-[11px] font-semibold text-ink-500 mb-1 block">Гости</label>
            <select value={awayId} onChange={e => { setAwayId(Number(e.target.value)); setResult(null) }}
              className="w-full bg-ink-50 border border-ink-200 text-ink-800 rounded-xl px-3 py-2.5 text-[13px] focus:border-brand-400 focus:ring-1 focus:ring-brand-200 focus:outline-none">
              <option value={0}>Выберите команду</option>
              {teams.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
            </select>
          </div>
        </div>
        <button onClick={doPrediction} disabled={!homeId || !awayId || homeId === awayId || loading}
          className="mt-3 mx-auto flex items-center gap-2 px-6 py-2.5 bg-brand-600 hover:bg-brand-700 disabled:bg-ink-200 disabled:text-ink-400 text-white text-[13px] font-semibold rounded-xl transition-colors shadow-sm">
          {loading ? <div className="w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin" /> : <Zap className="w-4 h-4" />}
          Рассчитать
        </button>
      </div>

      {result && (
        <>
          {/* Outcome probabilities */}
          <div className="bg-white rounded-2xl shadow-card border border-ink-100/60 p-5">
            <div className="flex items-center gap-3 mb-4">
              <div className="flex-1 text-center">
                <div className="text-[11px] text-ink-400 mb-0.5">{homeTeam?.name ?? 'Хозяева'}</div>
                <div className="text-[28px] font-black font-mono text-emerald-600">{(parseFloat(result.homeWinProb) * 100).toFixed(1)}%</div>
              </div>
              <div className="flex-1 text-center">
                <div className="text-[11px] text-ink-400 mb-0.5">Ничья</div>
                <div className="text-[28px] font-black font-mono text-amber-500">{(parseFloat(result.drawProb) * 100).toFixed(1)}%</div>
              </div>
              <div className="flex-1 text-center">
                <div className="text-[11px] text-ink-400 mb-0.5">{awayTeam?.name ?? 'Гости'}</div>
                <div className="text-[28px] font-black font-mono text-rose-500">{(parseFloat(result.awayWinProb) * 100).toFixed(1)}%</div>
              </div>
            </div>
            <div className="h-2.5 rounded-full overflow-hidden flex">
              <div className="bg-emerald-500 transition-all" style={{ width: `${parseFloat(result.homeWinProb) * 100}%` }} />
              <div className="bg-amber-400 transition-all" style={{ width: `${parseFloat(result.drawProb) * 100}%` }} />
              <div className="bg-rose-500 transition-all" style={{ width: `${parseFloat(result.awayWinProb) * 100}%` }} />
            </div>
            <div className="mt-4 text-center">
              <span className="text-[12px] text-ink-400">Прогноз: </span>
              <span className="font-mono text-[20px] font-black text-ink-900">{result.predictedScore}</span>
            </div>
          </div>

          {/* Poisson */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {[
              { label: 'Пуассон (хозяева)', lam: parseFloat(result.lambdaHome), sym: '\u03bb', color: '#7048e8', colorDim: '#d4c9fe' },
              { label: 'Пуассон (гости)', lam: parseFloat(result.muAway), sym: '\u03bc', color: '#9171f8', colorDim: '#e9e3ff' },
            ].map((ch) => (
              <div key={ch.label} className="bg-white rounded-2xl shadow-card border border-ink-100/60 p-4">
                <div className="flex items-center justify-between mb-3">
                  <span className="text-[12px] font-semibold text-ink-600">{ch.label}</span>
                  <span className="text-[11px] font-mono text-ink-400">{ch.sym} = {ch.lam.toFixed(3)}</span>
                </div>
                <ResponsiveContainer width="100%" height={150}>
                  <BarChart data={poisson(ch.lam, 6)}>
                    <XAxis dataKey="goals" tick={{ fill: '#6b6b8a', fontSize: 11 }} axisLine={false} tickLine={false} />
                    <YAxis tick={{ fill: '#9090aa', fontSize: 10 }} axisLine={false} tickLine={false} unit="%" width={30} />
                    <Tooltip contentStyle={tooltipStyle} formatter={(v: any) => [`${v}%`, 'P']} />
                    <Bar dataKey="prob" radius={[5, 5, 0, 0]}>
                      {poisson(ch.lam, 6).map((_, i) => (
                        <Cell key={i} fill={i === Math.round(ch.lam) ? ch.color : ch.colorDim} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            ))}
          </div>

          {/* Matrix */}
          {result.scoreMatrix && (
            <div className="bg-white rounded-2xl shadow-card border border-ink-100/60 p-5">
              <h2 className="text-[13px] font-semibold text-ink-700 mb-3">Матрица вероятностей</h2>
              <ScoreMatrix matrix={result.scoreMatrix} lambdaHome={parseFloat(result.lambdaHome)} muAway={parseFloat(result.muAway)} />
            </div>
          )}
        </>
      )}
    </div>
  )
}
