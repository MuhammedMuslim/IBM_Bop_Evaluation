import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api'
import { RequestTable } from './EmployeeDashboard'

export default function ManagerDashboard() {
  const [pending, setPending] = useState([])
  const [schedule, setSchedule] = useState([])
  const [team, setTeam] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([
      api('/api/leave-requests/pending'),
      api('/api/leave-requests/team/schedule'),
      api('/api/users/team'),
    ])
      .then(([p, s, t]) => {
        setPending(p)
        setSchedule(s)
        setTeam(t)
      })
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div className="stack">
      <div>
        <h1>Manager dashboard</h1>
        <p className="muted">Approve requests and track team availability.</p>
      </div>
      {error && <div className="error">{error}</div>}
      <div className="grid grid-3">
        <div className="card stat">
          <div className="muted">Pending approvals</div>
          <strong>{pending.length}</strong>
        </div>
        <div className="card stat">
          <div className="muted">Team members</div>
          <strong>{team.length}</strong>
        </div>
        <div className="card stat">
          <div className="muted">On leave this period</div>
          <strong>{schedule.length}</strong>
        </div>
      </div>
      <div className="card stack">
        <div className="row" style={{ justifyContent: 'space-between' }}>
          <h2>Needs decision</h2>
          <Link className="btn" to="/approvals">
            Open approvals
          </Link>
        </div>
        <RequestTable rows={pending.slice(0, 5)} />
      </div>
    </div>
  )
}
