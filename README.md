# Campus Placement System

A full-stack web application connecting students with campus recruiters, built with **Spring Boot 3.3** and **Vanilla JS**.

---

## Features

| Role | Capabilities |
|------|-------------|
| **Student** | Browse/search jobs, apply with resume, track application status |
| **Recruiter** | Post/edit/delete jobs, view applicants, update application status |
| **Admin** | View all users and jobs, remove any account or job posting |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.3, Spring Security (JWT), Spring Data JPA |
| Database | MySQL 8 |
| Frontend | Vanilla JS, Bootstrap 5.3 |
| Auth | Stateless JWT (JJWT 0.12.6) |
| File Storage | Local filesystem (configurable path) |

---

## Prerequisites

- Java 21+
- Maven 3.8+
- MySQL 8 running locally

---

## Setup (Local Development)

### 1. Clone the repository
```bash
git clone <repo-url>
cd campus-placement-system
```

### 2. Create the MySQL schema
```sql
CREATE DATABASE campus_placement_db;
```

### 3. Set environment variables

The application reads sensitive values from environment variables — **never hardcode credentials**.

**Windows (PowerShell):**
```powershell
$env:DB_PASSWORD = "your_mysql_password"
$env:JWT_SECRET = "a-random-256-bit-base64-string-at-least-32-chars"
$env:ADMIN_DEFAULT_PASSWORD = "SecureAdminPass@123"
```

**Linux/macOS:**
```bash
export DB_PASSWORD="your_mysql_password"
export JWT_SECRET="a-random-256-bit-base64-string"
export ADMIN_DEFAULT_PASSWORD="SecureAdminPass@123"
```

Or create a `.env` file (see `.env.example`) and source it.

### 4. Run with the dev profile (enables SQL logging)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 5. Open in browser
```
http://localhost:8080
```
- Default admin: `admin@campusplacement.com` / `<ADMIN_DEFAULT_PASSWORD>`

---

## API Summary

### Auth (public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register (STUDENT or RECRUITER) |
| POST | `/api/auth/login` | Login → returns JWT |

### Jobs (paginated, search)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/jobs?keyword=&location=&page=0` | Public | Browse/search jobs |
| POST | `/api/jobs` | Recruiter | Post a new job |
| PUT | `/api/jobs/{id}` | Recruiter (owner) | Edit job |
| DELETE | `/api/jobs/{id}` | Recruiter (owner) / Admin | Delete job |

### Applications
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/applications/apply/{jobId}` | Student | Apply to a job |
| GET | `/api/applications/my?page=0` | Student | My applications |
| GET | `/api/applications/job/{jobId}?page=0` | Recruiter/Admin | Applicants list |
| PUT | `/api/applications/{id}/status` | Recruiter/Admin | Update status |

### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/stats` | Platform statistics |
| GET/DELETE | `/api/admin/students/{id}` | List / remove student |
| GET/DELETE | `/api/admin/recruiters/{id}` | List / remove recruiter |
| GET/DELETE | `/api/admin/jobs/{id}` | List / remove job |

---

## Security Notes

- JWT tokens expire in **1 hour** (`jwt.expiration-ms=3600000`)
- Passwords require 8+ characters with uppercase, lowercase, and digit
- Resume files: PDF-only, 5 MB limit, magic-byte validated
- All user-supplied HTML is escaped on the frontend to prevent XSS
- Admin accounts are seeded at startup only — not self-registrable

---

## Running Tests

```bash
mvn test
```

---

## Environment Variables Reference

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_URL` | No | `jdbc:mysql://localhost:3306/campus_placement_db?createDatabaseIfNotExist=true` | Full JDBC URL |
| `DB_USERNAME` | No | `root` | MySQL username |
| `DB_PASSWORD` | **Yes** | — | MySQL password |
| `JWT_SECRET` | **Yes** | — | HMAC-SHA256 signing key (32+ chars) |
| `ADMIN_DEFAULT_PASSWORD` | **Yes** (first run) | — | Initial admin account password |
