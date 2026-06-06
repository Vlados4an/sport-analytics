import { useState } from 'react'
import { Database, RefreshCw, CheckCircle, AlertCircle, Clock } from 'lucide-react'
import clsx from 'clsx'
import { seedData, recalculateRatings } from '../api/client'

interface LogEntry { time: string; msg: string; type: 'ok' | 'err' | 'info' }

export default function AdminPage() {
  const [logs, setLogs] = useState<LogEntry[]>([])
  const [seedL, setSeedL] = useState(false)
  const [ratL, setRatL] = useState(false)

  const log = (msg: string, type: LogEntry['type'] = 'info') =>
    setLogs(p => [{ time: new Date().toLocaleTimeString(), msg, type }, ...p])

  const doSeed = async () => {
    setSeedL(true); log('Загрузка данных...')
    try { const r = await seedData(); log(JSON.stringify(r), 'ok') }
    catch (e: any) { log(e?.response?.data?.error || e.message, 'err') }
    finally { setSeedL(false) }
  }

  const doRatings = async () => {
    setRatL(true); log('Пересчёт рейтингов...')
    try { const r = await recalculateRatings(); log(JSON.stringify(r), 'ok') }
    catch (e: any) { log(e?.response?.data?.error || e.message, 'err') }
    finally { setRatL(false) }
  }

  const services = [
    { name: 'PostgreSQL', desc: 'OLTP данные', color: 'text-sky-600 bg-sky-50' },
    { name: 'ClickHouse', desc: 'OLAP метрики', color: 'text-amber-600 bg-amber-50' },
    { name: 'Redis', desc: 'Кэш', color: 'text-rose-600 bg-rose-50' },
  ]

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h1 className="text-[28px] font-bold text-ink-900 tracking-tight">Администрирование</h1>
        <p className="text-ink-400 text-sm">Управление данными</p>
      </div>

      {/* Services */}
      <div className="grid grid-cols-3 gap-3">
        {services.map(s => (
          <div key={s.name} className="bg-white rounded-2xl shadow-card border border-ink-100/60 p-4">
            <div className="flex items-center gap-2 mb-2">
              <div className={clsx('w-7 h-7 rounded-lg flex items-center justify-center', s.color)}>
                <Database className="w-3.5 h-3.5" />
              </div>
              <span className="text-[13px] font-semibold text-ink-700">{s.name}</span>
            </div>
            <div className="flex items-center gap-1.5">
              <div className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
              <span className="text-[11px] text-emerald-600 font-medium">Online</span>
            </div>
          </div>
        ))}
      </div>

      {/* Actions */}
      <div className="bg-white rounded-2xl shadow-card border border-ink-100/60 p-5">
        <h2 className="text-[13px] font-semibold text-ink-700 mb-3">Действия</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <button onClick={doSeed} disabled={seedL}
            className="flex items-center gap-3 p-3.5 rounded-xl border border-ink-200 hover:border-brand-300 hover:bg-brand-50/50 transition-all text-left">
            {seedL ? <div className="w-5 h-5 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" /> : <Database className="w-5 h-5 text-brand-600" />}
            <div>
              <div className="text-[13px] font-medium text-ink-800">Загрузить данные</div>
              <div className="text-[11px] text-ink-400">10 команд, 90 матчей</div>
            </div>
          </button>
          <button onClick={doRatings} disabled={ratL}
            className="flex items-center gap-3 p-3.5 rounded-xl border border-ink-200 hover:border-brand-300 hover:bg-brand-50/50 transition-all text-left">
            {ratL ? <div className="w-5 h-5 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" /> : <RefreshCw className="w-5 h-5 text-amber-600" />}
            <div>
              <div className="text-[13px] font-medium text-ink-800">Пересчитать рейтинги</div>
              <div className="text-[11px] text-ink-400">I<sub>a</sub>, I<sub>d</sub>, F &rarr; R</div>
            </div>
          </button>
        </div>
      </div>

      {/* Log */}
      <div className="bg-white rounded-2xl shadow-card border border-ink-100/60 p-5">
        <h2 className="text-[13px] font-semibold text-ink-700 mb-3">Журнал</h2>
        {logs.length === 0 ? (
          <p className="text-[12px] text-ink-400 text-center py-6">Нет событий</p>
        ) : (
          <div className="space-y-1.5 max-h-52 overflow-y-auto">
            {logs.map((l, i) => (
              <div key={i} className={clsx('flex items-start gap-2 px-3 py-2 rounded-lg text-[12px]',
                l.type === 'ok' && 'bg-emerald-50 text-emerald-700',
                l.type === 'err' && 'bg-rose-50 text-rose-700',
                l.type === 'info' && 'bg-ink-50 text-ink-600',
              )}>
                {l.type === 'ok' && <CheckCircle className="w-3.5 h-3.5 mt-0.5 shrink-0" />}
                {l.type === 'err' && <AlertCircle className="w-3.5 h-3.5 mt-0.5 shrink-0" />}
                {l.type === 'info' && <Clock className="w-3.5 h-3.5 mt-0.5 shrink-0" />}
                <span className="flex-1 break-all">{l.msg}</span>
                <span className="text-[10px] opacity-50 shrink-0 font-mono">{l.time}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
