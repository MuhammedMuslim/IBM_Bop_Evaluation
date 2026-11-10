import { useEffect, useState } from 'react'
import { api } from '../api'

const empty = {
  email: '',
  password: 'Password123!',
  firstName: '',
  lastName: '',
  employeeCode: '',
  department: '',
  jobTitle: '',
  role: 'EMPLOYEE',
  managerId: '',
}

export default function Employees() {
  const [users, setUsers] = useState([])
  const [form, setForm] = useState(empty)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  async function load() {
    setUsers(await api('/api/users'))
  }

  useEffect(() => {
    load().catch((err) => setError(err.message))
  }, [])

  async function create(e) {
    e.preventDefault()
    setError('')
    setMessage('')
    try {
      await api('/api/users', {
        method: 'POST',
        body: JSON.stringify({
          ...form,
          managerId: form.managerId ? Number(form.managerId) : null,
        }),
      })
      setForm(empty)
      setMessage('Employee created.')
      await load()
    } catch (err) {
      setError(err.message)
    }
  }

  const managers = users.filter((u) => u.role === 'MANAGER' || u.role === 'HR_ADMIN')

  return (
    <div className="stack">
      <h1>Employees</h1>
      {error && <div className="error">{error}</div>}
      {message && <div className="success">{message}</div>}
      <form className="card stack" onSubmit={create}>
        <h2>Create employee</h2>
        <div className="grid grid-2">
          {['firstName', 'lastName', 'email', 'password', 'employeeCode', 'department', 'jobTitle'].map(
            (key) => (
              <label className="field" key={key}>
                <span>{key}</span>
                <input
                  type={key === 'password' ? 'password' : 'text'}
                  value={form[key]}
                  onChange={(e) => setForm({ ...form, [key]: e.target.value })}
                  required={['firstName', 'lastName', 'email', 'password'].includes(key)}
                />
              </label>
            ),
          )}
          <label className="field">
            <span>Role</span>
            <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
              <option value="EMPLOYEE">EMPLOYEE</option>
              <option value="MANAGER">MANAGER</option>
              <option value="HR_ADMIN">HR_ADMIN</option>
              <option value="SYSTEM_ADMIN">SYSTEM_ADMIN</option>
            </select>
          </label>
          <label className="field">
            <span>Manager</span>
            <select
              value={form.managerId}
              onChange={(e) => setForm({ ...form, managerId: e.target.value })}
            >
              <option value="">None</option>
              {managers.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.firstName} {m.lastName}
                </option>
              ))}
            </select>
          </label>
        </div>
        <button className="btn" type="submit">
          Create
        </button>
      </form>
      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Manager</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>
                  {u.firstName} {u.lastName}
                </td>
                <td>{u.email}</td>
                <td>{u.role}</td>
                <td>{u.managerName || '—'}</td>
                <td>{u.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
