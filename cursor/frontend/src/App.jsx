import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './AuthContext'
import Layout from './components/Layout'
import AllRequests from './pages/AllRequests'
import Approvals from './pages/Approvals'
import Audit from './pages/Audit'
import Balances from './pages/Balances'
import Employees from './pages/Employees'
import History from './pages/History'
import Home from './pages/Home'
import LeaveTypes from './pages/LeaveTypes'
import Login from './pages/Login'
import Notifications from './pages/Notifications'
import Policies from './pages/Policies'
import RequestLeave from './pages/RequestLeave'
import Team from './pages/Team'

function Protected({ children }) {
  const { user, loading } = useAuth()
  if (loading) return <div className="page">Loading…</div>
  if (!user) return <Navigate to="/login" replace />
  return children
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <Protected>
            <Layout />
          </Protected>
        }
      >
        <Route index element={<Home />} />
        <Route path="request-leave" element={<RequestLeave />} />
        <Route path="history" element={<History />} />
        <Route path="notifications" element={<Notifications />} />
        <Route path="approvals" element={<Approvals />} />
        <Route path="team" element={<Team />} />
        <Route path="employees" element={<Employees />} />
        <Route path="leave-types" element={<LeaveTypes />} />
        <Route path="policies" element={<Policies />} />
        <Route path="balances" element={<Balances />} />
        <Route path="requests" element={<AllRequests />} />
        <Route path="audit" element={<Audit />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
