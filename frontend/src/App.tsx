import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom'
import Layout from './components/Layout'
import DashboardPage from './pages/DashboardPage'
import MatchesPage from './pages/MatchesPage'
import MatchDetailPage from './pages/MatchDetailPage'
import RatingsPage from './pages/RatingsPage'
import PredictionsPage from './pages/PredictionsPage'
import LoginPage from './pages/LoginPage'
import AdminPage from './pages/AdminPage'
import { isAuthenticated } from './api/client'

function RequireAuth() {
  if (!isAuthenticated()) return <Navigate to="/login" replace />
  return <Outlet />
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="login" element={<LoginPage />} />
          <Route element={<RequireAuth />}>
            <Route index element={<DashboardPage />} />
            <Route path="matches" element={<MatchesPage />} />
            <Route path="matches/:id" element={<MatchDetailPage />} />
            <Route path="ratings" element={<RatingsPage />} />
            <Route path="predictions" element={<PredictionsPage />} />
            <Route path="admin" element={<AdminPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
