import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api'

export default function HrDashboard() {
  const [summary, setSummary] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api('/api/reports/summary')
      .then(setSummary)
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div className="stack">
      <div>
        <h1>HR dashboard</h1>
        <p className="muted">Organization leave health at a glance.</p>
      </div>
      {error && <div className="error">{error}</div>}
      {summary && (
        <div className="grid grid-4">
          <div className="card stat">
            <div className="muted">Active employees</div>
            <strong>{summary.totalEmployees}</strong>
          </div>
          <div className="card stat">
            <div className="muted">Pending</div>
            <strong>{summary.pendingRequests}</strong>
          </div>
          <div className="card stat">
            <div className="muted">Approved</div>
            <strong>{summary.approvedRequests}</strong>
          </div>
          <div className="card stat">
            <div className="muted">Rejected</div>
            <strong>{summary.rejectedRequests}</strong>
          </div>
        </div>
      )}
      <div className="grid grid-3">
        <Link className="card" to="/employees">
          <h2>Employees</h2>
          <p className="muted">Create profiles and assign managers.</p>
        </Link>
        <Link className="card" to="/policies">
          <h2>Policies</h2>
          <p className="muted">Configure entitlements and rules.</p>
        </Link>
        <Link className="card" to="/audit">
          <h2>Audit trail</h2>
          <p className="muted">Review critical leave actions.</p>
        </Link>
      </div>
    </div>
  )
}
