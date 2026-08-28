import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function ProtectedRoute() {
  const { user, isRestoring } = useAuth()
  const location = useLocation()

  if (isRestoring) return <div className="status-card">로그인 정보를 확인하고 있습니다.</div>
  if (!user) return <Navigate to="/login" state={{ from: location.pathname }} replace />
  return <Outlet />
}
