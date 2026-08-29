import { useEffect, useState } from 'react'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'

export default function Leaves() {
  const { user } = useAuth()
  const isReviewer = user.role === 'MANAGER' || user.role === 'ADMIN'

  const [myLeaves, setMyLeaves] = useState([])
  const [teamLeaves, setTeamLeaves] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [formError, setFormError] = useState('')
  const [formSuccess, setFormSuccess] = useState('')

  const [form, setForm] = useState({
    leaveType: 'CASUAL',
    startDate: '',
    endDate: '',
    reason: ''
  })

  useEffect(() => {
    loadLeaves()
  }, [])

  async function loadLeaves() {
    setLoading(true)
    try {
      const mine = await api.get('/leaves/me')
      setMyLeaves(mine.data)

      if (isReviewer) {
        const endpoint = user.role === 'ADMIN' ? '/leaves/all' : '/leaves/team'
        const team = await api.get(endpoint)
        setTeamLeaves(team.data)
      }
    } catch (err) {
      setError('Could not load leave requests')
    } finally {
      setLoading(false)
    }
  }

  async function handleApply(e) {
    e.preventDefault()
    setFormError('')
    setFormSuccess('')

    if (form.endDate < form.startDate) {
      setFormError('End date cannot be before start date')
      return
    }

    try {
      await api.post('/leaves', form)
      setFormSuccess('Leave request submitted!')
      setForm({ leaveType: 'CASUAL', startDate: '', endDate: '', reason: '' })
      loadLeaves()
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to submit leave request')
    }
  }

  async function handleReview(id, decision) {
    try {
      await api.put(`/leaves/${id}/${decision}`)
      loadLeaves()
    } catch (err) {
      alert(err.response?.data?.message || 'Action failed')
    }
  }

  return (
    <div className="page">
      <h1>Leave Management</h1>

      <div className="card">
        <h2>Apply for Leave</h2>
        <form className="inline-form" onSubmit={handleApply}>
          {formError && <div className="error-banner">{formError}</div>}
          {formSuccess && <div className="success-banner">{formSuccess}</div>}

          <label>Leave Type</label>
          <select
            value={form.leaveType}
            onChange={(e) => setForm({ ...form, leaveType: e.target.value })}
          >
            <option value="CASUAL">Casual</option>
            <option value="SICK">Sick</option>
            <option value="EARNED">Earned</option>
          </select>

          <label>Start Date</label>
          <input
            type="date"
            value={form.startDate}
            onChange={(e) => setForm({ ...form, startDate: e.target.value })}
            required
          />

          <label>End Date</label>
          <input
            type="date"
            value={form.endDate}
            onChange={(e) => setForm({ ...form, endDate: e.target.value })}
            required
          />

          <label>Reason</label>
          <textarea
            value={form.reason}
            onChange={(e) => setForm({ ...form, reason: e.target.value })}
            placeholder="Optional"
          />

          <button type="submit">Submit Request</button>
        </form>
      </div>

      <div className="card">
        <h2>My Leave Requests</h2>
        {loading && <p>Loading...</p>}
        {error && <p className="error-banner">{error}</p>}
        {!loading && myLeaves.length === 0 && <p>No leave requests yet.</p>}
        {!loading && myLeaves.length > 0 && (
          <table className="data-table">
            <thead>
              <tr><th>Type</th><th>Start</th><th>End</th><th>Status</th></tr>
            </thead>
            <tbody>
              {myLeaves.map((l) => (
                <tr key={l.id}>
                  <td>{l.leaveType}</td>
                  <td>{l.startDate}</td>
                  <td>{l.endDate}</td>
                  <td><StatusBadge status={l.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {isReviewer && (
        <div className="card">
          <h2>{user.role === 'ADMIN' ? 'All Leave Requests' : "My Team's Leave Requests"}</h2>
          {teamLeaves.length === 0 && <p>No requests to review.</p>}
          {teamLeaves.length > 0 && (
            <table className="data-table">
              <thead>
                <tr><th>Employee</th><th>Type</th><th>Start</th><th>End</th><th>Status</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {teamLeaves.map((l) => (
                  <tr key={l.id}>
                    <td>{l.employee?.user?.name}</td>
                    <td>{l.leaveType}</td>
                    <td>{l.startDate}</td>
                    <td>{l.endDate}</td>
                    <td><StatusBadge status={l.status} /></td>
                    <td>
                      {l.status === 'PENDING' && (
                        <>
                          <button className="btn-small" onClick={() => handleReview(l.id, 'approve')}>Approve</button>
                          <button className="btn-small secondary" onClick={() => handleReview(l.id, 'reject')}>Reject</button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  )
}

function StatusBadge({ status }) {
  const cls = status === 'APPROVED' ? 'badge-green' : status === 'REJECTED' ? 'badge-red' : 'badge-gray'
  return <span className={`badge ${cls}`}>{status}</span>
}
