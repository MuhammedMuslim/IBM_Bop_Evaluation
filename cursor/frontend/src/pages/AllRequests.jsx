import { useEffect, useState } from 'react'
import { api } from '../api'
import { RequestTable } from './EmployeeDashboard'

export default function AllRequests() {
  const [rows, setRows] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    api('/api/leave-requests')
      .then(setRows)
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div className="stack">
      <h1>All leave requests</h1>
      {error && <div className="error">{error}</div>}
      <div className="card">
        <RequestTable rows={rows} />
      </div>
    </div>
  )
}
