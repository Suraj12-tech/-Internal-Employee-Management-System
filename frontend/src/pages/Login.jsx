import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const { login } = useAuth()
  const navigate = useNavigate()

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(email, password)
      navigate('/dashboard')
    } catch (err) {
      // Backend sends a generic "Invalid email or password" message on purpose,
      // so we never reveal WHICH part was wrong.
      setError(err.response?.data?.message || 'Login failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>Employee Management System</h1>
        <p className="subtitle">Sign in to continue</p>

        {error && <div className="error-banner">{error}</div>}

        <label>Email</label>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="admin@cruvels.com"
          required
        />

        <label>Password</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="••••••••"
          required
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Signing in...' : 'Login'}
        </button>

        <div className="demo-hint">
          <strong>Demo accounts:</strong>
          <div>Admin: admin@cruvels.com / Admin@123</div>
          <div>Manager: manager@cruvels.com / Manager@123</div>
          <div>Employee: priya@cruvels.com / Employee@123</div>
        </div>
      </form>
    </div>
  )
}
