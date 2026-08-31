import { useEffect, useState } from 'react'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'

export default function Dashboard() {
  const { user } = useAuth()
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/dashboard/summary')
      .then((res) => setStats(res.data))
      .catch(() => setError('Could not load dashboard stats'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="page"><p>Loading dashboard...</p></div>
  if (error) return <div className="page"><p className="error-banner">{error}</p></div>

  const entries = Object.entries(stats || {})

  return (
    <div className="page">
      <h1>Welcome, {user.name}</h1>
      <p className="subtitle">Here's your {user.role.toLowerCase()} overview</p>

      {entries.length === 0 ? (
        <p>No stats to show yet.</p>
      ) : (
        <div className="stats-grid">
          {entries.map(([key, value]) => (
            <div className="stat-card" key={key}>
              <div className="stat-value">{value}</div>
              <div className="stat-label">{formatLabel(key)}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

function formatLabel(key) {
  const withSpaces = key.replace(/([A-Z])/g, ' $1')
  return withSpaces.charAt(0).toUpperCase() + withSpaces.slice(1)
}
