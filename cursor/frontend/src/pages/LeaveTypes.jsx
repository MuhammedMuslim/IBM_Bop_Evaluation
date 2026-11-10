import { useEffect, useState } from 'react'
import { api } from '../api'

export default function LeaveTypes() {
  const [rows, setRows] = useState([])
  const [form, setForm] = useState({
    name: '',
    description: '',
    paid: true,
    requiresApproval: true,
    active: true,
  })
  const [error, setError] = useState('')

  async function load() {
    setRows(await api('/api/leave-types?activeOnly=false'))
  }

  useEffect(() => {
    load().catch((err) => setError(err.message))
  }, [])

  async function create(e) {
    e.preventDefault()
    try {
      await api('/api/leave-types', { method: 'POST', body: JSON.stringify(form) })
      setForm({ name: '', description: '', paid: true, requiresApproval: true, active: true })
      await load()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="stack">
      <h1>Leave types</h1>
      {error && <div className="error">{error}</div>}
      <form className="card stack" onSubmit={create}>
        <div className="grid grid-2">
          <label className="field">
            <span>Name</span>
            <input
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
            />
          </label>
          <label className="field">
            <span>Description</span>
            <input
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </label>
        </div>
        <div className="row">
          <label>
            <input
              type="checkbox"
              checked={form.paid}
              onChange={(e) => setForm({ ...form, paid: e.target.checked })}
            />{' '}
            Paid
          </label>
          <label>
            <input
              type="checkbox"
              checked={form.requiresApproval}
              onChange={(e) => setForm({ ...form, requiresApproval: e.target.checked })}
            />{' '}
            Requires approval
          </label>
        </div>
        <button className="btn" type="submit">
          Add leave type
        </button>
      </form>
      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Paid</th>
              <th>Approval</th>
              <th>Active</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id}>
                <td>{r.name}</td>
                <td>{r.paid ? 'Yes' : 'No'}</td>
                <td>{r.requiresApproval ? 'Yes' : 'No'}</td>
                <td>{r.active ? 'Yes' : 'No'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
