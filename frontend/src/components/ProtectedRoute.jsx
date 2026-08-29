import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

// Wraps a page and redirects to /login if nobody is logged in.
// If "roles" is given, it also blocks users whose role isn't in that list
// (frontend-level UX only - the REAL enforcement always happens on the backend).
export default function ProtectedRoute({ children, roles }) {
  const { user } = useAuth()

  if (!user) {
    return <Navigate to="/login" replace />
  }

  if (roles && !roles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />
  }

  return children
}
