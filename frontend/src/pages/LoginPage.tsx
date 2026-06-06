import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { LogIn, UserPlus } from 'lucide-react'
import { login, register } from '../api/client'

export default function LoginPage() {
  const nav = useNavigate()
  const [isReg, setIsReg] = useState(false)
  const [loginName, setLoginName] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      if (isReg) await register(loginName, password)
      await login(loginName, password)
      nav('/')
    } catch (err: any) {
      setError(err?.response?.data?.error || 'Ошибка авторизации')
    } finally { setLoading(false) }
  }

  return (
    <div className="min-h-[75vh] flex items-center justify-center">
      <div className="w-full max-w-sm">
        <div className="text-center mb-6">
          <div className="w-12 h-12 rounded-xl bg-brand-600 flex items-center justify-center mx-auto mb-3">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
          </div>
          <h1 className="text-[22px] font-bold text-ink-900">
            Sport<span className="text-brand-600">Analytics</span>
          </h1>
          <p className="text-ink-400 text-[13px] mt-0.5">{isReg ? 'Новый аккаунт' : 'Вход в систему'}</p>
        </div>

        <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-card border border-ink-100/60 p-6 space-y-4">
          <div>
            <label className="text-[11px] font-semibold text-ink-500 mb-1 block">Логин</label>
            <input type="text" value={loginName} onChange={e => setLoginName(e.target.value)} required minLength={3}
              className="w-full bg-ink-50 border border-ink-200 text-ink-800 rounded-xl px-3.5 py-2.5 text-[13px] focus:border-brand-400 focus:ring-1 focus:ring-brand-200 focus:outline-none"
              placeholder="admin" />
          </div>
          <div>
            <label className="text-[11px] font-semibold text-ink-500 mb-1 block">Пароль</label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} required minLength={6}
              className="w-full bg-ink-50 border border-ink-200 text-ink-800 rounded-xl px-3.5 py-2.5 text-[13px] focus:border-brand-400 focus:ring-1 focus:ring-brand-200 focus:outline-none"
              placeholder="••••••" />
          </div>

          {error && <div className="text-[12px] text-rose-600 bg-rose-50 border border-rose-200 rounded-lg px-3 py-2">{error}</div>}

          <button type="submit" disabled={loading}
            className="w-full py-2.5 bg-brand-600 hover:bg-brand-700 disabled:bg-ink-200 text-white text-[13px] font-semibold rounded-xl transition-colors flex items-center justify-center gap-2 shadow-sm">
            {loading ? <div className="w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin" /> : isReg ? <UserPlus className="w-4 h-4" /> : <LogIn className="w-4 h-4" />}
            {isReg ? 'Создать аккаунт' : 'Войти'}
          </button>

          <p className="text-center text-[12px] text-ink-400">
            <button type="button" onClick={() => { setIsReg(!isReg); setError('') }} className="text-brand-600 hover:text-brand-700 font-medium">
              {isReg ? 'Уже есть аккаунт? Войти' : 'Создать аккаунт'}
            </button>
          </p>
        </form>
      </div>
    </div>
  )
}
