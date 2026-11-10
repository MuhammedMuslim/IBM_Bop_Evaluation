import { useEffect, useState } from 'react'
import { api } from '../api'

export default function Balances() {
  const [rows, setRows] = useState([])
  const [users, setUsers] = useState([])
  const [types, setTypes] = useState([])
  const [form, setForm] = useState({
    employeeId: '',
    leaveTypeId: '',
    year: new Date().getFullYear(),
    entitled: 20,
    adjustment: 0,
    reason: '',
  })
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  async function load() {
    const [b, u, t] = await Promise.all([
      api('/api/leave-balances'),
      api('/api/users'),
      api('/api/leave-types?activeOnly=true'),
    ])
    setRows(b)
    setUsers(u)
    setTypes(t)
    setForm((f) => ({
      ...f,
      employeeId: f.employeeId || (u[0] ? String(u[0].id) : ''),
      leaveTypeId: f.leaveTypeId || (t[0] ? String(t[0].id) : ''),
    }))
  }

  useEffect(() => {
    load().catch((err) => setError(err.message))
  }, [])

  async function adjust(e) {
    e.preventDefault()
    setError('')
    setMessage('')
    try {
      await api('/api/leave-balances/adjust', {
        method: 'POST',
        body: JSON.stringify({
          employeeId: Number(form.employeeId),
          leaveTypeId: Number(form.leaveTypeId),
          year: Number(form.year),
          entitled: Number(form.entitled),
          adjustment: Number(form.adjustment),
          reason: form.reason,
        }),
      })
      setMessage('Balance updated.')
      setForm((f) => ({ ...f, reason: '' }))
      await load()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="stack">
      <h1>Leave balances</h1>
      {error && <div className="error">{error}</div>}
      {message && <div className="success">{message}</div>}
      <form className="card stack" onSubmit={adjust}>
        <h2>Adjust entitlement</h2>
        <div className="grid grid-2">
          <label className="field">
            <span>Employee</span>
            <select
              value={form.employeeId}
              onChange={(e) => setForm({ ...form, employeeId: e.target.value })}
            >
              {users.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.firstName} {u.lastName}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Leave type</span>
            <select
              value={form.leaveTypeId}
              onChange={(e) => setForm({ ...form, leaveTypeId: e.target.value })}
            >
              {types.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.name}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Year</span>
            <input
              type="number"
              value={form.year}
              onChange={(e) => setForm({ ...form, year: e.target.value })}
            />
          </label>
          <label className="field">
            <span>Entitled</span>
            <input
              type="number"
              step="0.5"
              value={form.entitled}
              onChange={(e) => setForm({ ...form, entitled: e.target.value })}
            />
          </label>
          <label className="field">
            <span>Adjustment</span>
            <input
              type="number"
              step="0.5"
              value={form.adjustment}
              onChange={(e) => setForm({ ...form, adjustment: e.target.value })}
            />
          </label>
          <label className="field">
            <span>Reason</span>
            <input
              value={form.reason}
              onChange={(e) => setForm({ ...form, reason: e.target.value })}
              required
            />
          </label>
        </div>
        <button className="btn" type="submit">
          Save adjustment
        </button>
      </form>
      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Employee</th>
              <th>Type</th>
              <th>Year</th>
              <th>Entitled</th>
              <th>Used</th>
              <th>Pending</th>
              <th>Adj.</th>
              <th>Available</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id}>
                <td>{r.employeeName}</td>
                <td>{r.leaveTypeName}</td>
                <td>{r.year}</td>
                <td>{r.entitled}</td>
                <td>{r.used}</td>
                <td>{r.pending}</td>
                <td>{r.adjustment}</td>
                <td>{r.available}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
