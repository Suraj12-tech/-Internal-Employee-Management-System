import { useEffect, useState } from 'react'
import api from '../api/axios'

export default function Attendance() {
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadAttendance()
  }, [])

  function loadAttendance() {
    setLoading(true)
    api.get('/attendance/me')
      .then((res) => setRecords(res.data))
      .catch(() => setError('Could not load attendance history'))
      .finally(() => setLoading(false))
  }

  async function handleCheckIn() {
    setMessage('')
    setError('')
    try {
      await api.post('/attendance/checkin')
      setMessage('Checked in successfully!')
      loadAttendance()
    } catch (err) {
      // Handles the "duplicate check-in" case with a clear message from the backend
      setError(err.response?.data?.message || 'Check-in failed')
    }
  }

  async function handleCheckOut() {
    setMessage('')
    setError('')
    try {
      await api.post('/attendance/checkout')
      setMessage('Checked out successfully!')
      loadAttendance()
    } catch (err) {
      setError(err.response?.data?.message || 'Check-out failed')
    }
  }

  return (
    <div className="page">
      <h1>My Attendance</h1>

      <div className="action-row">
        <button onClick={handleCheckIn}>Check In</button>
        <button onClick={handleCheckOut} className="secondary">Check Out</button>
      </div>

      {message && <p className="success-banner">{message}</p>}
      {error && <p className="error-banner">{error}</p>}

      {loading && <p>Loading history...</p>}
      {!loading && records.length === 0 && <p>No attendance records yet.</p>}

      {!loading && records.length > 0 && (
        <table className="data-table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Check In</th>
              <th>Check Out</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {records.map((r) => (
              <tr key={r.id}>
                <td>{r.date}</td>
                <td>{r.checkInTime ? new Date(r.checkInTime).toLocaleTimeString() : '-'}</td>
                <td>{r.checkOutTime ? new Date(r.checkOutTime).toLocaleTimeString() : '-'}</td>
                <td>{r.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
