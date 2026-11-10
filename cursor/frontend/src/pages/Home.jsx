import { useAuth } from '../AuthContext'
import EmployeeDashboard from './EmployeeDashboard'
import HrDashboard from './HrDashboard'
import ManagerDashboard from './ManagerDashboard'

export default function Home() {
  const { user } = useAuth()
  if (user.role === 'MANAGER') return <ManagerDashboard />
  if (user.role === 'HR_ADMIN' || user.role === 'SYSTEM_ADMIN') return <HrDashboard />
  return <EmployeeDashboard />
}
