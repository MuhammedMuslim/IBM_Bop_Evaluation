import { useEffect, useState } from 'react'
import { api } from '../api'
import { RequestTable } from './EmployeeDashboard'

export default function Approvals() {
  const [rows, setRows] = useState([])
  const [comment, setComment] = useState({})
  const [error, setError] = useState('')

  async function load() {
    setRows(await api('/api/leave-requests/pending'))
  }

  useEffect(() => {
    load().catch((err) => setError(err.message))
  }, [])

  async function decide(id, approve) {
    setError('')
    try {
      await api(`/api/leave-requests/${id}/${approve ? 'approve' : 'reject'}`, {
        method: 'POST',
        body: JSON.stringify({ comment: comment[id] || '' }),
      })
      await load()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="stack">
      <h1>Pending approvals</h1>
      {error && <div className="error">{error}</div>}
      <div className="card">
        <RequestTable
          rows={rows}
          actions={(r) => (
            <div className="stack">
              <input
                placeholder="Comment"
                value={comment[r.id] || ''}
                onChange={(e) => setComment({ ...comment, [r.id]: e.target.value })}
              />
              <div className="row">
                <button className="btn" type="button" onClick={() => decide(r.id, true)}>
                  Approve
                </button>
                <button className="btn danger" type="button" onClick={() => decide(r.id, false)}>
                  Reject
                </button>
              </div>
            </div>
          )}
        />
      </div>
    </div>
  )
}
