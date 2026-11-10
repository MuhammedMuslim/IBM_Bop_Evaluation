import { useEffect, useState } from 'react'
import { api } from '../api'
import { RequestTable } from './EmployeeDashboard'

export default function History() {
  const [rows, setRows] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    api('/api/leave-requests/me')
      .then(setRows)
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div className="stack">
      <h1>Leave history</h1>
      {error && <div className="error">{error}</div>}
      <div className="card">
        <RequestTable rows={rows} />
      </div>
    </div>
  )
}
