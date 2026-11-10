# Employee Leave Management System (ELMS)

A full-stack web application implementing the ELMS Business Requirements Document (v1.0).

## Tech Stack

| Layer      | Technology                                      |
|------------|-------------------------------------------------|
| Frontend   | React 18 + TypeScript + React Router v6         |
| Backend    | Java 17 + Spring Boot 3.2 + Spring Security     |
| Database   | PostgreSQL 16 with Flyway migrations            |
| Auth       | JWT (HS256) + RBAC                              |
| Email      | Spring Mail + MailHog (dev)                     |
| Container  | Docker + Docker Compose                         |

---

## Architecture

```
elms/
├── backend/                   # Spring Boot REST API
│   ├── src/main/java/com/elms/app/
│   │   ├── config/            # SecurityConfig, CORS
│   │   ├── controller/        # REST endpoints
│   │   ├── dto/               # Request/Response DTOs
│   │   ├── entity/            # JPA entities
│   │   ├── enums/             # Role, LeaveStatus
│   │   ├── exception/         # Global error handler
│   │   ├── repository/        # Spring Data JPA repos
│   │   ├── security/          # JWT utils, filters
│   │   └── service/           # Business logic
│   └── src/main/resources/
│       ├── db/migration/      # Flyway SQL migrations
│       └── application.yml
├── frontend/                  # React SPA
│   └── src/
│       ├── api/               # Axios client + service functions
│       ├── auth/              # AuthContext, RequireAuth guard
│       ├── components/        # Shared UI components
│       ├── pages/             # Per-role page components
│       └── types/             # TypeScript interfaces
└── docker-compose.yml
```

---

## User Roles & Capabilities

| Role          | Capabilities                                                                 |
|---------------|------------------------------------------------------------------------------|
| EMPLOYEE      | View balances, submit leave, view/cancel own requests                        |
| MANAGER       | All employee features + approve/reject team requests, view team balances     |
| HR_ADMIN      | Full CRUD on employees, leave types, policies, balances; all reports         |
| SYSTEM_ADMIN  | HR_ADMIN capabilities + user management                                      |

---

## Quick Start

### Option A — Docker Compose (Recommended)

```bash
cd elms
docker-compose up --build
```

- Frontend:  http://localhost:3000
- Backend:   http://localhost:8080
- MailHog:   http://localhost:8025

### Option B — Local Development

**Prerequisites:** JDK 17+, Maven 3.9+, Node 20+, PostgreSQL 16

**Backend:**
```bash
cd backend
# Create DB: createdb elms_db
# Copy & configure: cp .env.example .env
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
cp .env.example .env
npm install
npm start
```

---

## Default Credentials (seeded)

| Email              | Password   | Role         |
|--------------------|------------|--------------|
| admin@elms.com     | Admin@123  | SYSTEM_ADMIN |
| hr@elms.com        | Admin@123  | HR_ADMIN     |

> Create MANAGER and EMPLOYEE accounts via HR Admin → Employees.

---

## REST API Overview

### Auth
| Method | Endpoint         | Description        |
|--------|------------------|--------------------|
| POST   | /api/auth/login  | Login → JWT token  |
| GET    | /api/auth/me     | Current user info  |

### Employee
| Method | Endpoint                    | Description              |
|--------|-----------------------------|--------------------------|
| GET    | /api/employee/balances      | My leave balances        |
| POST   | /api/leave-requests         | Submit leave request     |
| GET    | /api/leave-requests/my      | My request history       |
| PATCH  | /api/leave-requests/:id/cancel | Cancel a request      |

### Manager
| Method | Endpoint                                  | Description              |
|--------|-------------------------------------------|--------------------------|
| GET    | /api/manager/requests/pending             | Pending team requests    |
| GET    | /api/manager/requests                     | All team requests        |
| PATCH  | /api/manager/requests/:id/review          | Approve or reject        |
| GET    | /api/manager/team                         | Team members             |
| GET    | /api/manager/employees/:id/balances       | Employee balances        |

### HR Admin
| Method | Endpoint                         | Description              |
|--------|----------------------------------|--------------------------|
| GET    | /api/hr/employees                | All employees            |
| POST   | /api/hr/employees                | Create employee          |
| PUT    | /api/hr/employees/:id            | Update employee          |
| PATCH  | /api/hr/employees/:id/activate   | Activate employee        |
| PATCH  | /api/hr/employees/:id/deactivate | Deactivate employee      |
| GET/POST/PUT | /api/hr/leave-types       | Manage leave types       |
| GET/POST/PUT | /api/hr/policies          | Manage leave policies    |
| GET    | /api/hr/employees/:id/balances   | Employee balances        |
| POST   | /api/hr/balances/adjust          | Adjust leave balance     |
| POST   | /api/hr/balances/bulk-assign     | Bulk assign entitlements |
| GET    | /api/hr/leave-requests           | All leave requests       |
| GET    | /api/hr/audit-logs               | Paginated audit trail    |

---

## Business Rules Implemented

| Rule     | Description                                                         | Status |
|----------|---------------------------------------------------------------------|--------|
| RULE-01  | Authentication required                                             | ✅     |
| RULE-02  | Start date ≤ end date                                               | ✅     |
| RULE-03  | No past dates                                                       | ✅     |
| RULE-04  | Sufficient leave balance check                                      | ✅     |
| RULE-05  | No overlapping active requests                                      | ✅     |
| RULE-06  | Cannot approve own requests (enforced by RULE-07)                   | ✅     |
| RULE-07  | Only assigned manager can approve/reject                            | ✅     |
| RULE-08  | Only PENDING requests can be approved/rejected                      | ✅     |
| RULE-09  | Approved leave deducts balance                                      | ✅     |
| RULE-10  | Rejected leave does not affect balance                              | ✅     |
| RULE-11  | Cancellation of approved leave restores balance                     | ✅     |
| RULE-12  | HR balance adjustments require a reason                             | ✅     |
| RULE-13  | Weekends and public holidays excluded per policy                    | ✅     |
| RULE-14  | Atomic balance updates with pessimistic locking                     | ✅     |

---

## Database Schema

```
users ──────────────── leave_balances
  │                        │
  │                    leave_types ──── leave_policies
  │
  └── leave_requests ──── leave_types
          │
          └── audit_logs
              balance_adjustments
              public_holidays
```
