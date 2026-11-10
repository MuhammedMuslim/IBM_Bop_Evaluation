# Employee Leave Management System (ELMS)

Full-stack leave management app based on the ELMS BRD.

- **Frontend:** React (Vite)
- **Backend:** Java Spring Boot (REST, layered architecture)
- **Database:** PostgreSQL

## Features

- JWT authentication and role-based access (Employee, Manager, HR Admin, System Admin)
- Leave request submission with balance and overlap validation
- Manager approve / reject with comments
- Leave cancellation with balance restore
- Leave types, policies, entitlements, and balance adjustments
- Notifications (in-app; email optional)
- HR reporting summary and audit trail

## Quick start

### 1. Start PostgreSQL

```bash
docker compose up -d
```

### 2. Start the API

```bash
cd backend
mvn spring-boot:run
```

API: `http://localhost:8081`

### 3. Start the UI

```bash
cd frontend
npm install
npm run dev
```

UI: `http://localhost:5173`

## Demo accounts

Password for all seeded users: `Password123!`

| Email | Role |
| --- | --- |
| `alice@elms.local` | Employee |
| `bob@elms.local` | Employee |
| `manager@elms.local` | Manager |
| `hr@elms.local` | HR Admin |
| `admin@elms.local` | System Admin |

## Business rules covered

- Authenticated leave submission
- Start ≤ end, no past start dates
- Insufficient balance blocked
- Overlapping pending/approved requests blocked
- Employees cannot approve their own requests
- Only assigned manager (or HR/admin) can decide
- Only pending requests can be approved/rejected
- Approval deducts balance; rejection does not
- Cancellation restores balance per policy
- HR balance adjustments require a reason
- Weekends/public holidays excluded from duration per policy
- Optimistic locking (`@Version`) on leave balances
