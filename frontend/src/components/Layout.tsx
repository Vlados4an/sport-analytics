import { Link, Outlet, useLocation } from 'react-router-dom'
import {
  LayoutDashboard,
  Trophy,
  Swords,
  TrendingUp,
  Shield,
  LogIn,
} from 'lucide-react'
import clsx from 'clsx'

const TABS = [
  { to: '/', label: 'Главная', icon: LayoutDashboard },
  { to: '/matches', label: 'Матчи', icon: Swords },
  { to: '/ratings', label: 'Рейтинги', icon: Trophy },
  { to: '/predictions', label: 'Прогнозы', icon: TrendingUp },
  { to: '/admin', label: 'Админ', icon: Shield },
]

export default function Layout() {
  const { pathname } = useLocation()

  return (
    <div className="min-h-screen flex flex-col">
      {/* Header */}
      <header className="sticky top-0 z-40 bg-white/80 backdrop-blur-xl border-b border-ink-100">
        <div className="max-w-6xl mx-auto px-4 sm:px-6">
          <div className="h-14 flex items-center justify-between">
            <Link to="/" className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-brand-600 flex items-center justify-center">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
              </div>
              <span className="text-[15px] font-bold text-ink-900 tracking-tight">
                Sport<span className="text-brand-600">Analytics</span>
              </span>
            </Link>

            {/* Desktop nav */}
            <nav className="hidden md:flex items-center gap-1">
              {TABS.map(({ to, label, icon: Icon }) => {
                const active = to === '/' ? pathname === '/' : pathname.startsWith(to)
                return (
                  <Link
                    key={to}
                    to={to}
                    className={clsx(
                      'flex items-center gap-1.5 px-3.5 py-2 rounded-lg text-[13px] font-medium transition-colors',
                      active
                        ? 'bg-brand-50 text-brand-700'
                        : 'text-ink-500 hover:text-ink-800 hover:bg-ink-50',
                    )}
                  >
                    <Icon className="w-4 h-4" />
                    {label}
                  </Link>
                )
              })}
            </nav>

            <Link
              to="/login"
              className="flex items-center gap-1.5 text-[13px] font-medium text-ink-500 hover:text-brand-600 transition-colors"
            >
              <LogIn className="w-4 h-4" />
              <span className="hidden sm:inline">Войти</span>
            </Link>
          </div>
        </div>
      </header>

      {/* Content */}
      <main className="flex-1 max-w-6xl mx-auto w-full px-4 sm:px-6 py-6">
        <Outlet />
      </main>

      {/* Mobile bottom nav */}
      <nav className="md:hidden fixed bottom-0 inset-x-0 z-40 bg-white/95 backdrop-blur-xl border-t border-ink-100 safe-area-pb">
        <div className="flex items-center justify-around h-14">
          {TABS.map(({ to, label, icon: Icon }) => {
            const active = to === '/' ? pathname === '/' : pathname.startsWith(to)
            return (
              <Link
                key={to}
                to={to}
                className={clsx(
                  'flex flex-col items-center gap-0.5 text-[10px] font-medium transition-colors',
                  active ? 'text-brand-600' : 'text-ink-400',
                )}
              >
                <Icon className="w-5 h-5" />
                {label}
              </Link>
            )
          })}
        </div>
      </nav>
    </div>
  )
}
