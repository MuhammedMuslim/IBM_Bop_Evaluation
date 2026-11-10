import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../AuthContext'

export default function EmployeeDashboard() {
  const { user } = useAuth()
  const [balances, setBalances] = useState([])
  const [requests, setRequests] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([api('/api/leave-balances/me'), api('/api/leave-requests/me')])
      .then(([b, r]) => {
        setBalances(b)
        setRequests(r.slice(0, 5))
      })
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div className="stack">
      <div>
        <h1>Welcome, {user.firstName}</h1>
        <p className="muted">Review your balances and recent leave activity.</p>
      </div>
      {error && <div className="error">{error}</div>}
      <div className="grid grid-3">
        {balances.map((b) => (
          <div className="card stat" key={b.id}>
            <div className="muted">{b.leaveTypeName}</div>
            <strong>{b.available}</strong>
            <div className="muted">
              Available · used {b.used} · pending {b.pending}
            </div>
          </div>
        ))}
      </div>
      <div className="card stack">
        <div className="row" style={{ justifyContent: 'space-between' }}>
          <h2>Recent requests</h2>
          <Link className="btn" to="/request-leave">
            New request
          </Link>
        </div>
        <RequestTable rows={requests} />
      </div>
    </div>
  )
}

export function RequestTable({ rows, actions }) {
  if (!rows?.length) {
    return <p className="muted">No leave requests yet.</p>
  }
  return (
    <table className="table">
      <thead>
        <tr>
          <th>Employee</th>
          <th>Type</th>
          <th>Dates</th>
          <th>Days</th>
          <th>Status</th>
          {actions && <th>Actions</th>}
        </tr>
      </thead>
      <tbody>
        {rows.map((r) => (
          <tr key={r.id}>
            <td>{r.employeeName}</td>
            <td>{r.leaveTypeName}</td>
            <td>
              {r.startDate} → {r.endDate}
            </td>
            <td>{r.durationDays}</td>
            <td>
              <span className={`badge ${r.status}`}>{r.status}</span>
            </td>
            {actions && <td>{actions(r)}</td>}
          </tr>
        ))}
      </tbody>
    </table>
  )
}
