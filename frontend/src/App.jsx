import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Navbar from './components/Navbar'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Employees from './pages/Employees'
import Attendance from './pages/Attendance'
import Leaves from './pages/Leaves'

function AppRoutes() {
  const { user } = useAuth()

  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/login" element={user ? <Navigate to="/dashboard" /> : <Login />} />

        <Route path="/dashboard" element={
          <ProtectedRoute><Dashboard /></ProtectedRoute>
        } />

        <Route path="/employees" element={
          <ProtectedRoute><Employees /></ProtectedRoute>
        } />

        <Route path="/attendance" element={
          <ProtectedRoute><Attendance /></ProtectedRoute>
        } />

        <Route path="/leaves" element={
          <ProtectedRoute><Leaves /></ProtectedRoute>
        } />

        <Route path="*" element={<Navigate to={user ? '/dashboard' : '/login'} />} />
      </Routes>
    </>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}
