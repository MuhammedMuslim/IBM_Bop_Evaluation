# Cursor Report

**Project:** Employee Leave Management System (ELMS)  
**Chat ID:** `935a057f-1965-4d1f-90aa-278e0086cc2f`  
**Date:** Monday, Nov 9, 2026  

> Note: Cursor does not expose official billing/token meters to the agent. Token figures below are local estimates.

---

## Time

| Metric | Value |
| --- | --- |
| Chat start | Mon Nov 9, **11:03 AM** (UTC+2) |
| Main build finished | ~**11:14 AM** (UTC+2) |
| Active implementation | **~11 minutes** |
| Wall time to usage request | ~**3 hours** (mostly idle after build) |

---

## Token Usage (Estimate Only)

| Metric | Approx. |
| --- | --- |
| Local transcript size | ~195 KB |
| Rough token estimate | **~45k–80k** total (input + output) |
| Estimation method | Transcript size (~chars/4); does not include full model context/cache overhead |

For exact numbers, check **Cursor Settings → Usage / Billing**.

---

## Resources Used / Produced

| Resource | Approx. |
| --- | --- |
| Source files created | ~90 files (~250 KB) |
| Backend build (`backend/target/`) | ~53 MB |
| Frontend `node_modules` | ~69 MB |
| Frontend build (`frontend/dist/`) | ~0.3 MB |
| Runtime services | PostgreSQL (Docker), Spring Boot on **8081**, Vite on **5173** |
| Model / agent | Composer (Cursor agent) |

---

## Deliverable Summary

- Full-stack ELMS from BRD: React + Spring Boot + PostgreSQL
- JWT auth and role-based dashboards (Employee, Manager, HR, Admin)
- Leave request / approve / reject / cancel with business rules
- Leave types, policies, balances, notifications, audit, reports
- Seeded demo users and README quick-start

### Demo Access

| Service | URL |
| --- | --- |
| UI | http://localhost:5173 |
| API | http://localhost:8081 |

Password for all seeded users: `Password123!`

| Email | Role |
| --- | --- |
| `alice@elms.local` | Employee |
| `bob@elms.local` | Employee |
| `manager@elms.local` | Manager |
| `hr@elms.local` | HR Admin |
| `admin@elms.local` | System Admin |
