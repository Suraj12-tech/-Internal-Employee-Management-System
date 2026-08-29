import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  if (!user) return null

  return (
    <nav className="navbar">
      <div className="navbar-brand">EMS</div>
      <div className="navbar-links">
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/employees">Employees</Link>
        <Link to="/attendance">Attendance</Link>
        <Link to="/leaves">Leaves</Link>
      </div>
      <div className="navbar-user">
        <span>{user.name} ({user.role})</span>
        <button onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  )
}
