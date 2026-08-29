import { createContext, useContext, useState } from 'react'
import api from '../api/axios'

// AuthContext lets any page in the app know "who is logged in right now"
// without having to pass that information down manually through every component.
const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user')
    return saved ? JSON.parse(saved) : null
  })

  async function login(email, password) {
    const response = await api.post('/auth/login', { email, password })
    const { token, userId, name, role } = response.data

    const loggedInUser = { userId, name, role }
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify(loggedInUser))
    setUser(loggedInUser)

    return loggedInUser
  }

  function logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// Custom hook so pages can just write: const { user, login, logout } = useAuth()
export function useAuth() {
  return useContext(AuthContext)
}
