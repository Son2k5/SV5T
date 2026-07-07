# Sinh Viên 5 Tốt (SV5T) - System Overview & Setup Guide

Welcome to the SV5T Management System. This repository contains the complete codebase for both the Spring Boot Backend API and the Nuxt 3 Frontend Web Application.

---

## 📂 Project Structure

```text
SV5T/
├── api/SinhVien5T/     # Spring Boot Java Backend API
└── frontend/           # Nuxt 3 Vue 3 Frontend Web App
```

---

## 🔒 Security Architecture

### 1. Fine-Grained Role-Based Access Control (RBAC)
Instead of coarse role checks (e.g., checking only `hasRole('ADMIN')`), the system implements fine-grained permissions checked via `@PreAuthorize`.
The list of granular permissions includes:
* `USER_MANAGE`: Create and delete users, change roles, assign permissions, and change statuses.
* `CAMPAIGN_MANAGE`: Create, modify, close/open selection campaigns.
* `CRITERIA_MANAGE`: Manage standards and grading criteria.
* `EVIDENCE_REVIEW`: Detail check, comment on, and approve/reject evidence uploaded by students.
* `SETTING_MANAGE`: Edit system settings and view the administration dashboard.
* `AUDIT_LOG_VIEW`: Fetch and export admin action history.

*Mentors* retain standard `EVIDENCE_REVIEW` access, while *Administrators* require the specific authorities above to interact with specific modules.

### 2. GDPR-Compliant Sensitive Data Masking
Personal identification numbers (like identity cards/CCCDs) are masked by default when returning profiles to the frontend (e.g. `********1234`).
* The validation pattern allows both raw (9 or 12 digits) and masked (`*****1234` or `********1234`) formats.
* The update logic detects masked inputs and bypasses database overwrite/uniqueness checks to prevent accidental modifications or false conflict reports.

### 3. Secure Session & Token Management
* **Secure Cookies**: Refresh tokens are returned in HTTP-only, secure, same-site Lax cookies. Session cookies default to `Secure=true` in production to prevent CSRF/XSS exploitation.
* **Separated Cryptographic Keys**: Deriving encryption keys for SMTP/Mail configs uses a dedicated key (`NOTIFICATION_ENCRYPTION_KEY`) separate from `JWT_SECRET` to comply with secret segregation standards.

---

## 📦 Database Migrations (Flyway)

The project uses **Flyway** for database migration control. 
Upon startup, Flyway checks the `src/main/resources/db/migration` directory:
1. `V1__drop_legacy_unique_indexes.sql`: Conditionally drops legacy/unique constraint indexes to avoid issues on target platforms.
2. `V2__backfill_and_modify_schema.sql`: Normalizes legacy schema columns (such as converts groups/levels columns to standard Varchar) and automatically seeds default permissions and grants them to existing admin accounts.

Backend `spring.jpa.hibernate.ddl-auto` is set to `validate` to ensure production safety and schema enforcement.

---

## ⚙️ Environment Configuration

### Backend Environment Variables (`.env`)
Create an `.env` file under `/api/SinhVien5T/` containing:
```properties
DB_URL=jdbc:mysql://localhost:3306/sinhvien5t?useSSL=false&serverTimezone=Asia/Bangkok
DB_USERNAME=root
DB_PASSWORD=yourpassword
JWT_SECRET=your_jwt_signing_key_at_least_32_bytes_base64
NOTIFICATION_ENCRYPTION_KEY=your_smtp_encryption_key_at_least_32_bytes_base64
COOKIE_SECURE=true
```

### Frontend Environment Variables (`.env`)
Create an `.env` file under `/frontend/` containing:
```properties
NUXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

---

## 🚀 Running the Project

### Spring Boot Backend
1. Make sure MySQL & Redis are running.
2. Navigate to `api/SinhVien5T`.
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Nuxt 3 Frontend
1. Navigate to `frontend`.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   npm run dev
   ```
