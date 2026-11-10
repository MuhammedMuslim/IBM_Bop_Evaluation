import { useEffect, useState } from 'react'
import { api } from '../api'

export default function Notifications() {
  const [items, setItems] = useState([])
  const [error, setError] = useState('')

  async function load() {
    setItems(await api('/api/notifications'))
  }

  useEffect(() => {
    load().catch((err) => setError(err.message))
  }, [])

  async function markRead(id) {
    await api(`/api/notifications/${id}/read`, { method: 'POST' })
    await load()
  }

  return (
    <div className="stack">
      <h1>Notifications</h1>
      {error && <div className="error">{error}</div>}
      <div className="stack">
        {items.map((n) => (
          <div className="card row" key={n.id} style={{ justifyContent: 'space-between' }}>
            <div>
              <strong>{n.title}</strong>
              <div className="muted">{n.message}</div>
              <div className="muted" style={{ fontSize: '0.85rem' }}>
                {new Date(n.createdAt).toLocaleString()}
              </div>
            </div>
            {!n.read && (
              <button className="btn secondary" type="button" onClick={() => markRead(n.id)}>
                Mark read
              </button>
            )}
          </div>
        ))}
        {!items.length && <p className="muted">No notifications.</p>}
      </div>
    </div>
  )
}
