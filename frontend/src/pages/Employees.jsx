import { useEffect, useState } from 'react'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'

export default function Employees() {
  const { user } = useAuth()
  const [employees, setEmployees] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')

  useEffect(() => {
    loadEmployees()
  }, [])

  function loadEmployees() {
    setLoading(true)
    api.get('/employees')
      .then((res) => setEmployees(res.data))
      .catch(() => setError('Could not load employees'))
      .finally(() => setLoading(false))
  }

  async function handleDeactivate(id) {
    if (!window.confirm('Deactivate this employee?')) return
    try {
      await api.delete(`/employees/${id}`)
      loadEmployees() // refresh the list after the change
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to deactivate employee')
    }
  }

  const filtered = employees.filter((e) =>
    e.name.toLowerCase().includes(search.toLowerCase()) ||
    (e.departmentName || '').toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="page">
      <h1>{user.role === 'ADMIN' ? 'All Employees' : user.role === 'MANAGER' ? 'My Team' : 'My Profile'}</h1>

      <input
        className="search-box"
        placeholder="Search by name or department..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />

      {loading && <p>Loading...</p>}
      {error && <p className="error-banner">{error}</p>}
      {!loading && !error && filtered.length === 0 && <p>No employees found.</p>}

      {!loading && filtered.length > 0 && (
        <table className="data-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Department</th>
              <th>Designation</th>
              <th>Joining Date</th>
              <th>Manager</th>
              <th>Status</th>
              {user.role === 'ADMIN' && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {filtered.map((emp) => (
              <tr key={emp.id}>
                <td>{emp.name}</td>
                <td>{emp.email}</td>
                <td>{emp.departmentName || '-'}</td>
                <td>{emp.designation || '-'}</td>
                <td>{emp.joiningDate || '-'}</td>
                <td>{emp.managerName || '-'}</td>
                <td>
                  <span className={`badge ${emp.employmentStatus === 'ACTIVE' ? 'badge-green' : 'badge-gray'}`}>
                    {emp.employmentStatus}
                  </span>
                </td>
                {user.role === 'ADMIN' && (
                  <td>
                    {emp.employmentStatus === 'ACTIVE' && (
                      <button className="btn-small" onClick={() => handleDeactivate(emp.id)}>
                        Deactivate
                      </button>
                    )}
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
