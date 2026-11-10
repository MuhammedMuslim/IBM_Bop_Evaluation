# IBM Bob Session Report

**Project:** Employee Leave Management System (ELMS)
**Session Date:** 2026-11-09
**Session Start:** 09:03 UTC
**Session End:** 11:59 UTC
**Total Elapsed Time:** ~3 hours

---

## Summary

A full-stack Employee Leave Management System was implemented from scratch based on a Business Requirements Document (ELMS_BRD.md), followed by iterative debugging to get the application running inside Docker Compose.

---

## Files Created

| Category | Files | Count |
|---|---|---|
| Java entities | `User`, `LeaveType`, `LeavePolicy`, `LeaveBalance`, `LeaveRequest`, `AuditLog`, `BalanceAdjustment`, `PublicHoliday` | 8 |
| Java repositories | `UserRepository`, `LeaveTypeRepository`, `LeavePolicyRepository`, `LeaveBalanceRepository`, `LeaveRequestRepository`, `AuditLogRepository`, `PublicHolidayRepository`, `BalanceAdjustmentRepository` | 8 |
| Java services | `LeaveRequestService`, `LeaveBalanceService`, `UserService`, `EmailService`, `AuditService`, `WorkingDaysCalculator` | 6 |
| Java controllers | `AuthController`, `LeaveRequestController`, `ManagerController`, `HrController`, `EmployeeController`, `LeaveTypeController` | 6 |
| Java security | `JwtUtils`, `JwtAuthFilter`, `UserDetailsServiceImpl` | 3 |
| Java config/exception | `SecurityConfig`, `GlobalExceptionHandler`, `BusinessException`, `ResourceNotFoundException` | 4 |
| Java DTOs/enums | `LoginRequest`, `AuthResponse`, `CreateUserRequest`, `UserDto`, `LeaveRequestDto`, `LeaveRequestResponse`, `ReviewRequest`, `LeaveBalanceDto`, `AdjustBalanceRequest`, `Role`, `LeaveStatus` | 11 |
| SQL migrations | `V1__init_schema.sql`, `V2__seed_data.sql`, `V3__fix_seed_passwords.sql` | 3 |
| React pages | `LoginPage`, `EmployeeDashboard`, `RequestLeavePage`, `LeaveHistoryPage`, `ManagerDashboard`, `PendingApprovalsPage`, `TeamHistoryPage`, `TeamPage`, `HrDashboard`, `EmployeesPage`, `LeaveTypesPage`, `PoliciesPage`, `BalancesPage`, `AllRequestsPage`, `AuditLogsPage` | 15 |
| React core | `App.tsx`, `AuthContext.tsx`, `RequireAuth.tsx`, `AppLayout.tsx`, `components/common/index.tsx` | 5 |
| React API layer | `axios.ts`, `services.ts`, `types/index.ts` | 3 |
| Infrastructure | `pom.xml`, `application.yml`, `docker-compose.yml`, `backend/Dockerfile`, `frontend/Dockerfile`, `.env.example` (×2) | 7 |
| Documentation | `README.md`, `IBM Bob Report.md` | 2 |
| **Total** | | **~81** |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18 + TypeScript + React Router v6 |
| Backend | Java 17 + Spring Boot 3.2 + Spring Security 6 |
| Database | PostgreSQL 16 + Flyway migrations |
| Authentication | JWT HS256 + RBAC (4 roles) |
| Email | Spring Mail + MailHog (dev) |
| Build | Maven 3.9 (backend) · npm / CRA (frontend) |
| Container | Docker + Docker Compose |

---

## Business Rules Implemented

| Rule | Description | Status |
|---|---|---|
| RULE-01 | Authentication required for all endpoints | ✅ |
| RULE-02 | Start date must be ≤ end date | ✅ |
| RULE-03 | No past dates allowed | ✅ |
| RULE-04 | Sufficient leave balance check before approval | ✅ |
| RULE-05 | No overlapping active leave requests | ✅ |
| RULE-06 | Cannot approve own request | ✅ |
| RULE-07 | Only assigned manager can approve/reject | ✅ |
| RULE-08 | Only PENDING requests can be reviewed | ✅ |
| RULE-09 | Approved leave deducts balance atomically | ✅ |
| RULE-10 | Rejected leave does not affect balance | ✅ |
| RULE-11 | Cancellation of approved leave restores balance | ✅ |
| RULE-12 | HR balance adjustments require a reason | ✅ |
| RULE-13 | Weekends and public holidays excluded per policy | ✅ |
| RULE-14 | Atomic balance updates with pessimistic locking | ✅ |

---

## Bugs Fixed (10 Total)

| # | Error | Root Cause | Fix |
|---|---|---|---|
| 1 | `npm ci` failed in Docker build | `react-scripts@5` has peer dep conflicts with React 19 | Changed to `npm ci --legacy-peer-deps` |
| 2 | Java compile error — lambda variable not final | `request` was reassigned after lambda capture | Introduced `final LeaveRequest requestSnapshot` alias |
| 3 | Port 1025 already allocated (MailHog) | Local service occupying the port | Remapped host port to `1026:1025` |
| 4 | Port 5432 already allocated (PostgreSQL) | Local PostgreSQL instance running | Remapped host port to `5433:5432` |
| 5 | Port 8080 already allocated (backend) | Another Java process on 8080 | Remapped to `8081:8080` |
| 6 | Port 8081 already allocated (backend) | Another Java process on 8081 | Remapped to `8082:8080` |
| 7 | `UnknownHostException: db` | `application.yml` had `localhost` hardcoded; env var override not wired | Changed URL to `${DB_URL:jdbc:postgresql://localhost:5432/elms_db}` |
| 8 | `Schema-validation: missing table [audit_logs]` | Stale Docker volume + `ddl-auto: validate` firing before Flyway | Changed `ddl-auto` to `none`; ran `docker-compose down -v` |
| 9 | Deprecated `PostgreSQLDialect` warning | Explicit dialect set in `application.yml` (not needed in Hibernate 6) | Removed the `dialect` property |
| 10 | Flyway never ran — `users` table missing | `flyway-core` dependency was missing from `pom.xml` | Added `org.flywaydb:flyway-core` to `pom.xml` |

---

## REST API Surface

| Scope | Method | Endpoint | Description |
|---|---|---|---|
| Auth | POST | `/api/auth/login` | Login → JWT |
| Auth | GET | `/api/auth/me` | Current user |
| Employee | GET | `/api/employee/balances` | My leave balances |
| Employee | POST | `/api/leave-requests` | Submit request |
| Employee | GET | `/api/leave-requests/my` | My history |
| Employee | PATCH | `/api/leave-requests/:id/cancel` | Cancel request |
| Manager | GET | `/api/manager/requests/pending` | Pending reviews |
| Manager | GET | `/api/manager/requests` | All team requests |
| Manager | PATCH | `/api/manager/requests/:id/review` | Approve / Reject |
| Manager | GET | `/api/manager/team` | Team members |
| Manager | GET | `/api/manager/employees/:id/balances` | Employee balances |
| HR | GET/POST/PUT | `/api/hr/employees` | Employee CRUD |
| HR | PATCH | `/api/hr/employees/:id/activate` | Activate employee |
| HR | PATCH | `/api/hr/employees/:id/deactivate` | Deactivate employee |
| HR | GET/POST/PUT | `/api/hr/leave-types` | Leave type management |
| HR | GET/POST/PUT | `/api/hr/policies` | Policy management |
| HR | GET | `/api/hr/employees/:id/balances` | View balances |
| HR | POST | `/api/hr/balances/adjust` | Adjust balance |
| HR | POST | `/api/hr/balances/bulk-assign` | Bulk assign by year |
| HR | GET | `/api/hr/leave-requests` | All requests |
| HR | GET | `/api/hr/audit-logs` | Paginated audit trail |

---

## Running Services

| Service | URL |
|---|---|
| React Frontend | http://localhost:3000 |
| Spring Boot API | http://localhost:8082/api |
| MailHog Web UI | http://localhost:8025 |
| PostgreSQL | localhost:5433 |

---

## Default Credentials

| Email | Password | Role |
|---|---|---|
| `admin@elms.com` | `Admin@123` | SYSTEM_ADMIN |
| `hr@elms.com` | `Admin@123` | HR_ADMIN |

---

## Quick Start

```bash
cd elms
docker-compose down -v          # wipe any stale volumes
docker-compose build --no-cache # fresh build
docker-compose up
```
