import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../AuthContext'

const linksByRole = {
  EMPLOYEE: [
    { to: '/', label: 'Dashboard' },
    { to: '/request-leave', label: 'Request Leave' },
    { to: '/history', label: 'History' },
    { to: '/notifications', label: 'Notifications' },
  ],
  MANAGER: [
    { to: '/', label: 'Dashboard' },
    { to: '/approvals', label: 'Approvals' },
    { to: '/team', label: 'Team' },
    { to: '/request-leave', label: 'My Leave' },
    { to: '/notifications', label: 'Notifications' },
  ],
  HR_ADMIN: [
    { to: '/', label: 'Dashboard' },
    { to: '/employees', label: 'Employees' },
    { to: '/leave-types', label: 'Leave Types' },
    { to: '/policies', label: 'Policies' },
    { to: '/balances', label: 'Balances' },
    { to: '/requests', label: 'All Requests' },
    { to: '/audit', label: 'Audit' },
  ],
  SYSTEM_ADMIN: [
    { to: '/', label: 'Dashboard' },
    { to: '/employees', label: 'Users' },
    { to: '/leave-types', label: 'Leave Types' },
    { to: '/policies', label: 'Policies' },
    { to: '/balances', label: 'Balances' },
    { to: '/requests', label: 'All Requests' },
    { to: '/audit', label: 'Audit' },
  ],
}

export default function Layout() {
  const { user, logout } = useAuth()
  const links = linksByRole[user?.role] || linksByRole.EMPLOYEE

  return (
    <div>
      <header className="nav">
        <div>
          <div className="brand">ELMS</div>
          <div className="muted" style={{ fontSize: '0.85rem' }}>
            {user?.firstName} {user?.lastName} · {user?.role?.replace('_', ' ')}
          </div>
        </div>
        <nav className="nav-links">
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} end={link.to === '/'}>
              {link.label}
            </NavLink>
          ))}
        </nav>
        <button className="btn secondary" onClick={logout} type="button">
          Sign out
        </button>
      </header>
      <main className="page">
        <Outlet />
      </main>
    </div>
  )
}
