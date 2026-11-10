import { useEffect, useState } from 'react'
import { api } from '../api'
import { RequestTable } from './EmployeeDashboard'

export default function RequestLeave() {
  const [types, setTypes] = useState([])
  const [requests, setRequests] = useState([])
  const [form, setForm] = useState({
    leaveTypeId: '',
    startDate: '',
    endDate: '',
    reason: '',
  })
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function load() {
    const [t, r] = await Promise.all([
      api('/api/leave-types?activeOnly=true'),
      api('/api/leave-requests/me'),
    ])
    setTypes(t)
    setRequests(r)
    if (!form.leaveTypeId && t[0]) {
      setForm((f) => ({ ...f, leaveTypeId: String(t[0].id) }))
    }
  }

  useEffect(() => {
    load().catch((err) => setError(err.message))
  }, [])

  async function submit(e) {
    e.preventDefault()
    setBusy(true)
    setError('')
    setMessage('')
    try {
      await api('/api/leave-requests', {
        method: 'POST',
        body: JSON.stringify({
          leaveTypeId: Number(form.leaveTypeId),
          startDate: form.startDate,
          endDate: form.endDate,
          reason: form.reason,
        }),
      })
      setMessage('Leave request submitted.')
      setForm((f) => ({ ...f, startDate: '', endDate: '', reason: '' }))
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function cancel(id) {
    setError('')
    try {
      await api(`/api/leave-requests/${id}/cancel`, { method: 'POST' })
      await load()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="stack">
      <div>
        <h1>Request leave</h1>
        <p className="muted">Balances are validated before the request is submitted.</p>
      </div>
      {error && <div className="error">{error}</div>}
      {message && <div className="success">{message}</div>}
      <form className="card stack" onSubmit={submit}>
        <div className="grid grid-2">
          <label className="field">
            <span>Leave type</span>
            <select
              value={form.leaveTypeId}
              onChange={(e) => setForm({ ...form, leaveTypeId: e.target.value })}
              required
            >
              {types.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.name}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Reason (optional)</span>
            <input
              value={form.reason}
              onChange={(e) => setForm({ ...form, reason: e.target.value })}
            />
          </label>
          <label className="field">
            <span>Start date</span>
            <input
              type="date"
              value={form.startDate}
              onChange={(e) => setForm({ ...form, startDate: e.target.value })}
              required
            />
          </label>
          <label className="field">
            <span>End date</span>
            <input
              type="date"
              value={form.endDate}
              onChange={(e) => setForm({ ...form, endDate: e.target.value })}
              required
            />
          </label>
        </div>
        <button className="btn" disabled={busy} type="submit">
          {busy ? 'Submitting…' : 'Submit request'}
        </button>
      </form>
      <div className="card stack">
        <h2>My requests</h2>
        <RequestTable
          rows={requests}
          actions={(r) =>
            (r.status === 'PENDING' || r.status === 'APPROVED') && (
              <button className="btn secondary" type="button" onClick={() => cancel(r.id)}>
                Cancel
              </button>
            )
          }
        />
      </div>
    </div>
  )
}
