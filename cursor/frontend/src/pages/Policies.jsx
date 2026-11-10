import { useEffect, useState } from 'react'
import { api } from '../api'

export default function Policies() {
  const [rows, setRows] = useState([])
  const [types, setTypes] = useState([])
  const [form, setForm] = useState({
    name: '',
    leaveTypeId: '',
    annualEntitlementDays: 20,
    excludeWeekends: true,
    excludePublicHolidays: true,
    allowNegativeBalance: false,
    restoreBalanceOnCancel: true,
    maxConsecutiveDays: 30,
    active: true,
  })
  const [error, setError] = useState('')

  async function load() {
    const [p, t] = await Promise.all([
      api('/api/leave-policies'),
      api('/api/leave-types?activeOnly=true'),
    ])
    setRows(p)
    setTypes(t)
    if (!form.leaveTypeId && t[0]) {
      setForm((f) => ({ ...f, leaveTypeId: String(t[0].id) }))
    }
  }

  useEffect(() => {
    load().catch((err) => setError(err.message))
  }, [])

  async function create(e) {
    e.preventDefault()
    try {
      await api('/api/leave-policies', {
        method: 'POST',
        body: JSON.stringify({
          ...form,
          leaveTypeId: Number(form.leaveTypeId),
          annualEntitlementDays: Number(form.annualEntitlementDays),
          maxConsecutiveDays: Number(form.maxConsecutiveDays),
        }),
      })
      await load()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="stack">
      <h1>Leave policies</h1>
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
            <span>Annual entitlement days</span>
            <input
              type="number"
              value={form.annualEntitlementDays}
              onChange={(e) => setForm({ ...form, annualEntitlementDays: e.target.value })}
            />
          </label>
          <label className="field">
            <span>Max consecutive days</span>
            <input
              type="number"
              value={form.maxConsecutiveDays}
              onChange={(e) => setForm({ ...form, maxConsecutiveDays: e.target.value })}
            />
          </label>
        </div>
        <button className="btn" type="submit">
          Add policy
        </button>
      </form>
      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Type</th>
              <th>Entitlement</th>
              <th>Exclude weekends</th>
              <th>Active</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id}>
                <td>{r.name}</td>
                <td>{r.leaveTypeName}</td>
                <td>{r.annualEntitlementDays}</td>
                <td>{r.excludeWeekends ? 'Yes' : 'No'}</td>
                <td>{r.active ? 'Yes' : 'No'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
