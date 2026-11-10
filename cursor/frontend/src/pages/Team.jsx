import { useEffect, useState } from 'react'
import { api } from '../api'
import { RequestTable } from './EmployeeDashboard'

export default function Team() {
  const [team, setTeam] = useState([])
  const [history, setHistory] = useState([])
  const [schedule, setSchedule] = useState([])
  const [selected, setSelected] = useState(null)
  const [balances, setBalances] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([
      api('/api/users/team'),
      api('/api/leave-requests/team'),
      api('/api/leave-requests/team/schedule'),
    ])
      .then(([t, h, s]) => {
        setTeam(t)
        setHistory(h)
        setSchedule(s)
      })
      .catch((err) => setError(err.message))
  }, [])

  async function showBalances(employeeId) {
    setSelected(employeeId)
    setBalances(await api(`/api/leave-balances/employee/${employeeId}`))
  }

  return (
    <div className="stack">
      <h1>Team overview</h1>
      {error && <div className="error">{error}</div>}
      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Department</th>
              <th>Title</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {team.map((u) => (
              <tr key={u.id}>
                <td>
                  {u.firstName} {u.lastName}
                </td>
                <td>{u.department}</td>
                <td>{u.jobTitle}</td>
                <td>
                  <button className="btn secondary" type="button" onClick={() => showBalances(u.id)}>
                    Balances
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {selected && (
        <div className="card">
          <h2>Leave balances</h2>
          <table className="table">
            <thead>
              <tr>
                <th>Type</th>
                <th>Available</th>
                <th>Used</th>
                <th>Pending</th>
              </tr>
            </thead>
            <tbody>
              {balances.map((b) => (
                <tr key={b.id}>
                  <td>{b.leaveTypeName}</td>
                  <td>{b.available}</td>
                  <td>{b.used}</td>
                  <td>{b.pending}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <div className="card stack">
        <h2>Team schedule (approved)</h2>
        <RequestTable rows={schedule} />
      </div>
      <div className="card stack">
        <h2>Team leave history</h2>
        <RequestTable rows={history} />
      </div>
    </div>
  )
}
