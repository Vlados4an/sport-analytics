import { useCallback, useEffect, useState } from 'react'
import { Database, RefreshCw, DownloadCloud, CheckCircle, AlertCircle, Clock } from 'lucide-react'
import clsx from 'clsx'
import { seedData, recalculateRatings, runEtl, fetchAdminEvents, fetchAdminStatus } from '../api/client'

interface ServerEvent {
  id: number
  eventType: string
  message: string | null
  status: string
  createdAt: string
}

interface AdminStatus {
  postgres?: { online: boolean; teams: number; matches: number }
  clickhouse?: { online: boolean; metricsRows: number }
  redis?: { online: boolean }
}

const EVENT_LABELS: Record<string, string> = {
  API_CALL: 'API',
  VALIDATION: 'Валидация',
  ETL: 'ETL',
  SYNC: 'ClickHouse',
  RATING: 'Рейтинги',
  SEED: 'Сид',
  CACHE: 'Кэш',
  AUTH: 'Auth',
}

export default function AdminPage() {
  const [events, setEvents] = useState<ServerEvent[]>([])
  const [status, setStatus] = useState<AdminStatus>({})
  const [seedL, setSeedL] = useState(false)
  const [ratL, setRatL] = useState(false)
  const [etlL, setEtlL] = useState(false)
  const [flash, setFlash] = useState<{ msg: string; type: 'ok' | 'err' } | null>(null)

  const refresh = useCallback(() => {
    fetchAdminEvents(30).then(setEvents).catch(() => {})
    fetchAdminStatus().then(setStatus).catch(() => {})
  }, [])

  useEffect(() => { refresh() }, [refresh])

  const run = async (
    action: () => Promise<unknown>,
    setLoading: (v: boolean) => void,
    okMsg: (r: any) => string,
  ) => {
    setLoading(true)
    setFlash(null)
    try {
      const r = await action()
      setFlash({ msg: okMsg(r), type: 'ok' })
    } catch (e: any) {
      setFlash({ msg: e?.response?.data?.error || e.message, type: 'err' })
    } finally {
      setLoading(false)
      refresh()
    }
  }

  const services = [
    {
      name: 'PostgreSQL',
      color: 'text-sky-600 bg-sky-50',
      online: status.postgres?.online,
      detail: status.postgres ? `${status.postgres.teams} команд · ${status.postgres.matches} матчей` : 'OLTP данные',
    },
    {
      name: 'ClickHouse',
      color: 'text-amber-600 bg-amber-50',
      online: status.clickhouse?.online,
      detail: status.clickhouse ? `${status.clickhouse.metricsRows} строк метрик (xG)` : 'OLAP метрики',
    },
    {
      name: 'Redis',
      color: 'text-rose-600 bg-rose-50',
      online: status.redis?.online,
      detail: 'Кэш (cache-aside)',
    },
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
              <div className={clsx('w-1.5 h-1.5 rounded-full', s.online === false ? 'bg-rose-500' : 'bg-emerald-500')} />
              <span className={clsx('text-[11px] font-medium', s.online === false ? 'text-rose-600' : 'text-emerald-600')}>
                {s.online === false ? 'Offline' : 'Online'}
              </span>
            </div>
            <div className="text-[10px] text-ink-400 mt-1">{s.detail}</div>
          </div>
        ))}
      </div>

      {/* Actions */}
      <div className="bg-white rounded-2xl shadow-card border border-ink-100/60 p-5">
        <h2 className="text-[13px] font-semibold text-ink-700 mb-3">Действия</h2>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <button
            onClick={() => run(seedData, setSeedL, (r) => r?.status || 'Данные загружены')}
            disabled={seedL}
            className="flex items-center gap-3 p-3.5 rounded-xl border border-ink-200 hover:border-brand-300 hover:bg-brand-50/50 transition-all text-left">
            {seedL ? <div className="w-5 h-5 border-2 border-brand-500 border-t-transparent rounded-full animate-spin shrink-0" /> : <Database className="w-5 h-5 text-brand-600 shrink-0" />}
            <div>
              <div className="text-[13px] font-medium text-ink-800">Начальная загрузка</div>
              <div className="text-[11px] text-ink-400">АПЛ 2024/25 · 20 команд</div>
            </div>
          </button>
          <button
            onClick={() => run(runEtl, setEtlL, (r) => r?.status || 'ETL выполнен')}
            disabled={etlL}
            className="flex items-center gap-3 p-3.5 rounded-xl border border-ink-200 hover:border-brand-300 hover:bg-brand-50/50 transition-all text-left">
            {etlL ? <div className="w-5 h-5 border-2 border-brand-500 border-t-transparent rounded-full animate-spin shrink-0" /> : <DownloadCloud className="w-5 h-5 text-emerald-600 shrink-0" />}
            <div>
              <div className="text-[13px] font-medium text-ink-800">Загрузить тур (ETL)</div>
              <div className="text-[11px] text-ink-400">API &rarr; PG &rarr; ClickHouse</div>
            </div>
          </button>
          <button
            onClick={() => run(recalculateRatings, setRatL, () => 'Рейтинги пересчитаны')}
            disabled={ratL}
            className="flex items-center gap-3 p-3.5 rounded-xl border border-ink-200 hover:border-brand-300 hover:bg-brand-50/50 transition-all text-left">
            {ratL ? <div className="w-5 h-5 border-2 border-brand-500 border-t-transparent rounded-full animate-spin shrink-0" /> : <RefreshCw className="w-5 h-5 text-amber-600 shrink-0" />}
            <div>
              <div className="text-[13px] font-medium text-ink-800">Пересчитать рейтинги</div>
              <div className="text-[11px] text-ink-400">I<sub>a</sub>, I<sub>d</sub>, F &rarr; R</div>
            </div>
          </button>
        </div>
        {flash && (
          <div className={clsx('mt-3 px-3 py-2 rounded-lg text-[12px] flex items-center gap-2',
            flash.type === 'ok' ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700')}>
            {flash.type === 'ok' ? <CheckCircle className="w-3.5 h-3.5 shrink-0" /> : <AlertCircle className="w-3.5 h-3.5 shrink-0" />}
            <span>{flash.msg}</span>
          </div>
        )}
      </div>

      {/* Event log (server-side, table event_log) */}
      <div className="bg-white rounded-2xl shadow-card border border-ink-100/60 p-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-[13px] font-semibold text-ink-700">Журнал событий</h2>
          <span className="text-[10px] text-ink-300 font-mono">event_log</span>
        </div>
        {events.length === 0 ? (
          <p className="text-[12px] text-ink-400 text-center py-6">Нет событий</p>
        ) : (
          <div className="space-y-1.5 max-h-72 overflow-y-auto">
            {events.map(e => (
              <div key={e.id} className={clsx('flex items-start gap-2 px-3 py-2 rounded-lg text-[12px]',
                e.status === 'ok' && 'bg-emerald-50 text-emerald-700',
                e.status === 'error' && 'bg-rose-50 text-rose-700',
                e.status !== 'ok' && e.status !== 'error' && 'bg-ink-50 text-ink-600',
              )}>
                {e.status === 'ok' && <CheckCircle className="w-3.5 h-3.5 mt-0.5 shrink-0" />}
                {e.status === 'error' && <AlertCircle className="w-3.5 h-3.5 mt-0.5 shrink-0" />}
                {e.status !== 'ok' && e.status !== 'error' && <Clock className="w-3.5 h-3.5 mt-0.5 shrink-0" />}
                <span className="font-semibold shrink-0">{EVENT_LABELS[e.eventType] || e.eventType}</span>
                <span className="flex-1 break-words">{e.message}</span>
                <span className="text-[10px] opacity-50 shrink-0 font-mono">
                  {new Date(e.createdAt).toLocaleTimeString()}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
