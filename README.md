# Employee Management System (EMS) — MVP

A focused MVP internal portal for managing employee records, attendance, and leave requests, built for the Cruvels Engineering Internship Technical Assignment (**Option D**).

---

## 1. Features

- **Authentication** — Login with email/password, JWT-based sessions, BCrypt password hashing
- **Authorization** — Three roles (Employee, Manager, Admin) enforced on the backend, not just hidden in the UI
- **Employee management** — Admin creates/views/deactivates employees; Managers see only their team
- **Attendance** — Check-in/check-out, duplicate-action prevention, history, team view for managers
- **Leave management** — Apply for leave, view status, manager/admin approve or reject, date-range validation
- **Dashboard** — Role-specific summary stats
- **Seed data** — Sample Admin, Manager, and 2 Employees created automatically on first run

---

## 2. Technology Stack

| Layer | Technology | Why |
|---|---|---|
| Backend | Java 21 + Spring Boot 3 | Mature ecosystem, strong typing, built-in Spring Security |
| Database | MySQL | Relational data fits well (employees, departments, hierarchy) |
| ORM | Spring Data JPA / Hibernate | Removes boilerplate SQL, keeps entities as plain Java classes |
| Auth | Spring Security + JWT (jjwt) | Stateless, scalable, industry-standard |
| Frontend | React 18 + Vite | Fast dev server, component-based UI |
| HTTP client | Axios | Interceptors handle auth token + 401 redirects automatically |
| Routing | React Router | Standard client-side routing with protected routes |

---

## 3. Architecture

```
[React Frontend :5173]
        |  (Axios, JWT in Authorization header)
        v
[Spring Boot REST API :8080]
        |
[Spring Security Filter Chain]  -> validates JWT -> sets authenticated user + role
        |
[Controller] -> [Service (business logic + authorization checks)] -> [Repository]
        |
[MySQL Database]
```

**Key decision:** authorization (who can see/edit what) is enforced in the **Service layer**, not the Controller. This keeps the rule "a Manager can only touch their own team's data" in one place per feature, testable independently of HTTP.

---

## 4. Database Design

### Entities & Relationships

- **User** — login credentials (email, hashed password, role). One-to-one with Employee.
- **Employee** — the work profile (department, designation, joining date, employment status).
- **Department** — one department has many employees.
- **Employee → Employee** (self-referencing `manager_id`) — models the reporting hierarchy.
- **Attendance** — one row per employee per day. `UNIQUE(employee_id, date)` constraint prevents duplicate check-ins at the database level.
- **LeaveRequest** — leave applications with type, date range, status, and who reviewed it.

```
User (1) ────── (1) Employee (M) ──── (1) Department
                     │  ▲
                     │  │ manager_id (self-reference)
                     │  │
                     ├──┘
                     │
        ┌────────────┼─────────────┐
        │                          │
  Attendance (M)            LeaveRequest (M)
```

### Notable constraints
- `users.email` — unique
- `attendance(employee_id, date)` — unique composite key (stops duplicate check-ins)
- Passwords are **BCrypt hashed**, never stored or returned in plain text
- `employees.user_id` — unique (enforces the one-to-one link)

---

## 5. Role Permissions

| Action | Employee | Manager | Admin |
|---|:---:|:---:|:---:|
| View own profile | ✅ | ✅ | ✅ |
| View/manage all employees | ❌ | ❌ (team only) | ✅ |
| Create employees | ❌ | ❌ | ✅ |
| Mark own attendance | ✅ | ✅ | ✅ |
| View team attendance | ❌ | ✅ (own team) | ✅ (all) |
| Apply for leave | ✅ | ✅ | ✅ |
| Approve/reject leave | ❌ | ✅ (own team only) | ✅ (all) |

All of the above is enforced with `@PreAuthorize` at the controller level **and** explicit ownership checks inside the service layer (e.g. `EmployeeService.assertCanView`, `LeaveService.reviewLeave`), so a Manager cannot approve leave for someone outside their team even if they guess the request URL.

---

## 6. API Documentation

Base URL: `http://localhost:8080/api`

### Auth
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/login` | Public | `{email, password}` → `{token, userId, name, role}` |
| POST | `/auth/logout` | Public | No-op (JWT is stateless; frontend deletes the token) |

### Employees
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/employees` | Admin | Create employee (creates login + profile together) |
| GET | `/employees` | Any | Admin: all · Manager: own team · Employee: self only |
| GET | `/employees/{id}` | Any | 403 if not self/team/admin |
| DELETE | `/employees/{id}` | Admin | Soft delete (sets status = INACTIVE) |

### Departments
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/departments` | Any | List all departments |
| POST | `/departments` | Admin | Create a department |

### Attendance
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/attendance/checkin` | Any | 400 if already checked in today |
| POST | `/attendance/checkout` | Any | 400 if no check-in exists yet |
| GET | `/attendance/me` | Any | Own attendance history |
| GET | `/attendance/employee/{id}` | Any (checked) | 403 if not self/manager/admin |

### Leaves
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/leaves` | Any | 400 if endDate < startDate |
| GET | `/leaves/me` | Any | Own leave requests |
| GET | `/leaves/team` | Manager/Admin | Team's requests |
| GET | `/leaves/all` | Admin | All requests |
| PUT | `/leaves/{id}/approve` | Manager/Admin | 403 if requester isn't on your team |
| PUT | `/leaves/{id}/reject` | Manager/Admin | Same rule as above |

### Dashboard
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/dashboard/summary` | Any | Returns role-appropriate stats |

**Example — successful login:**
```json
POST /api/auth/login
{ "email": "admin@cruvels.com", "password": "Admin@123" }

200 OK
{
  "token": "eyJhbGciOi...",
  "userId": 1,
  "name": "Asha Admin",
  "role": "ADMIN"
}
```

**Example — error response (consistent shape for every failure):**
```json
{
  "timestamp": "2026-08-28T10:15:00",
  "status": 403,
  "message": "You can only view your own team"
}
```

---

## 7. Local Setup

### Prerequisites
- Java 21+
- Maven (or use the included `mvnw` wrapper — not included here, install Maven separately)
- MySQL 8+ running locally
- Node.js 18+

### Backend

```bash
cd backend

# 1. Create the database (or let the app auto-create it via the JDBC URL)
mysql -u root -p -e "CREATE DATABASE ems_db;"

# 2. Set your DB password as an environment variable (or edit application.properties directly)
export DB_PASSWORD=your_mysql_password

# 3. Run the app
mvn spring-boot:run
```

Backend starts on **http://localhost:8080**. On first run, sample data is seeded automatically (see console output for demo logins).

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts on **http://localhost:5173**.

### Environment Variables

| Variable | Where | Purpose |
|---|---|---|
| `DB_PASSWORD` | backend | MySQL password (defaults to `root` if unset) |
| `JWT_SECRET` | backend | Signing key for JWT tokens (defaults to a dev-only value — **change in production**) |

---

## 8. Sample / Demo Credentials

Seeded automatically on first run:

| Role | Email | Password |
|---|---|---|
| Admin | admin@cruvels.com | Admin@123 |
| Manager | manager@cruvels.com | Manager@123 |
| Employee | priya@cruvels.com | Employee@123 |
| Employee | vikram@cruvels.com | Employee@123 |

---

## 9. Testing

Located in `backend/src/test/java/com/cruvels/ems/`. Run with:

```bash
cd backend
mvn test
```

| Test | Type | Covers |
|---|---|---|
| `EmsApplicationTests` | Context load | Spring app + security config wire up correctly |
| `LeaveServiceTest.applyLeave_shouldReject_whenEndDateBeforeStartDate` | Validation / failure | Invalid date range rejected |
| `LeaveServiceTest.applyLeave_shouldSucceed_withValidDateRange` | Success | Leave saved as PENDING |
| `LeaveServiceTest.reviewLeave_shouldBeDenied_whenManagerIsNotDirectManager` | Authorization | Manager can't approve outside their team |
| `AttendanceServiceTest.checkIn_shouldFail_whenAlreadyCheckedInToday` | Edge / duplicate action | Prevents double check-in |
| `AttendanceServiceTest.checkIn_shouldSucceed_whenNoRecordExistsForToday` | Success | First check-in of the day works |
| `AttendanceServiceTest.checkOut_shouldFail_whenNoCheckInExistsForToday` | Edge case | Can't check out without checking in |

Tests use Mockito to mock the repository layer, so they run fast without needing a live database.

---

## 10. Known Limitations

- No password reset / forgot-password flow
- No pagination on employee/leave lists (fine for MVP data volume)
- Leave balance / accrual is not tracked — leave requests can be applied without a running balance check
- No file upload (e.g. medical certificates for sick leave)
- Frontend role-based UI hiding is for UX only — actual security is on the backend

## 11. Future Improvements

- Leave balance tracking per employee
- Email notifications on leave approval/rejection
- Pagination and server-side filtering for large employee lists
- Audit log for admin actions
- Docker Compose setup for one-command local start
- Automated API docs (springdoc-openapi / Swagger UI)

---

## 12. Project Structure

```
employee-management-system/
├── backend/
│   └── src/main/java/com/cruvels/ems/
│       ├── model/          → JPA entities (User, Employee, Department, Attendance, LeaveRequest, enums)
│       ├── repository/     → Spring Data JPA interfaces
│       ├── service/        → business logic + authorization checks
│       ├── controller/     → REST endpoints
│       ├── dto/            → request/response objects (keeps entities out of the API surface)
│       ├── security/       → JWT util, CurrentUserProvider, UserDetailsService
│       ├── config/         → SecurityConfig, JwtAuthFilter
│       ├── exception/      → custom exceptions + GlobalExceptionHandler
│       └── DataSeeder.java → seeds demo data on first run
├── frontend/
│   └── src/
│       ├── api/axios.js        → shared Axios instance with JWT interceptor
│       ├── context/AuthContext → global "who is logged in" state
│       ├── components/         → Navbar, ProtectedRoute
│       └── pages/               → Login, Dashboard, Employees, Attendance, Leaves
└── README.md
```
