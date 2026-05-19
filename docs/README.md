# + HealthAssist — Healthcare Management System

> A full-stack desktop healthcare management application built with **JavaFX**, **Spring Boot**, and **MySQL**.

---

## Project Structure

```
health-assist/
├── backend/                     ← Spring Boot REST API (JDBC)
│   └── src/main/java/com/health/backend/
│       ├── config/              ← Database config, Swagger/OpenAPI
│       ├── controller/          ← REST endpoints
│       ├── service/             ← Business logic
│       ├── repository/          ← JDBC data access
│       ├── model/               ← Data models
│       ├── dto/                 ← Request/Response objects
│       └── exception/           ← Error handling
├── frontend/                    ← JavaFX Desktop UI
│   └── src/main/java/com/health/ui/
│       ├── app/                 ← Main entry point
│       ├── controllers/
│       │   ├── auth/            ← Login, Register
│       │   ├── patient/         ← Patient Dashboard
│       │   ├── doctor/          ← Doctor Dashboard
│       │   └── admin/           ← Admin Dashboard
│       ├── models/              ← Data transfer objects
│       ├── services/            ← API calls, ReminderService
│       └── utils/               ← SessionManager
├── database/
│   ├── schema.sql               ← 16 tables, fully normalized (3NF/BCNF)
│   └── seed.sql                 ← 8 test users (SHA-256 hashed passwords)
├── docs/
│   ├── API_DOCUMENTATION.md
│   └── api-docs.json            ← OpenAPI 3.0 specification
├── .env
└── README.md
```

---

## Prerequisites

| Software | Version |
|---|---|
| Java JDK | 22+ |
| MySQL | 8.0 |
| JavaFX SDK | 26 |
| IntelliJ IDEA | Community 2024+ |

---

## Quick Start

### 1. Database Setup

Open **MySQL Workbench** and run both scripts in order:

```sql
source database/schema.sql
source database/seed.sql
```

### 2. Configure Environment

Copy `.env` to `backend/.env` and set your MySQL password:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=health_assistance_system
DB_USERNAME=root
DB_PASSWORD=YourMySQLPasswordHere
JWT_SECRET=HealthAssistSystem2024SuperSecretKeyForJWT!!
JWT_EXPIRATION=86400000
SERVER_PORT=8080
```

### 3. Start the Backend

```bash
cd backend
./mvnw spring-boot:run
```

- Server: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### 4. Start the Frontend

1. Open the project in IntelliJ IDEA
2. Add VM options to your run configuration:
   ```
   --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics
   ```
3. Run `MainApp.java`

---

## Test Credentials

| Role | Full Name | Email | Password |
|---|---|---|---|
| **Admin** | Michael Abebe | `michael.abebe@healthassist.com` | `Admin@2024!Secure` |
| **Doctor** | Dr. Tesfaye Kebede | `dr.tesfaye.kebede@healthassist.com` | `Dr.Tesfaye#2024` |
| **Doctor** | Dr. Hanna Tadesse | `dr.hanna.tadesse@healthassist.com` | `Dr.Hanna#2024` |
| **Doctor** | Dr. Yonas Desta | `dr.yonas.desta@healthassist.com` | `Dr.Yonas#2024` |
| **Patient** | Rahel Asmamaw | `rahel.asmamaw@gmail.com` | `Rahel@2024!` |
| **Patient** | Abel Teshome | `abel.teshome@yahoo.com` | `Abel@2024!` |
| **Patient** | Mahlet Worku | `mahlet.worku@gmail.com` | `Mahlet@2024!` |
| **Patient** | Daniel Girma | `daniel.girma@outlook.com` | `Daniel@2024!` |

---

## Features

### Patient Dashboard
- View appointments with status badges (CONFIRMED / PENDING / CANCELLED)
- Book new appointments with available doctors
- View health records: diagnoses and prescriptions
- Monitor vitals with progress bars
- Update profile and view notifications

### Doctor Dashboard
- Today's appointments, total patients, and pending stats
- Confirm / cancel appointments → patient notified automatically
- Add health records (diagnosis, prescription) → patient notified
- View patient list with full history (records + vitals)
- Book appointments for patients, send custom notifications
- Search appointments, edit profile

### Admin Dashboard
- System-wide stats (patients, doctors, appointments, pending)
- View and delete users
- Confirm / cancel any appointment
- View all doctors with specializations

### System Features
- Appointment conflict prevention
- Background appointment reminders (multithreading)
- SHA-256 password hashing
- JWT token authentication
- Swagger / OpenAPI 3.0 documentation
- Dark theme UI with glassmorphism effects

---

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Login → JWT token |
| POST | `/api/auth/register` | Register user |
| GET | `/api/patients` | List all patients |
| GET | `/api/patients/{id}` | Get patient by ID |
| PUT | `/api/patients/{id}` | Update patient |
| GET | `/api/doctors` | List all doctors |
| GET | `/api/doctors/{id}` | Get doctor by ID |
| GET | `/api/doctors/{id}/patients` | Doctor's patient list |
| GET | `/api/doctors/{id}/stats` | Doctor stats |
| GET | `/api/appointments` | All appointments |
| GET | `/api/appointments/upcoming` | Upcoming appointments |
| GET | `/api/appointments/patient/{id}` | Patient's appointments |
| GET | `/api/appointments/doctor/{id}` | Doctor's appointments |
| POST | `/api/appointments` | Book appointment |
| PATCH | `/api/appointments/{id}/confirm` | Confirm appointment |
| PATCH | `/api/appointments/{id}/cancel` | Cancel appointment |
| GET | `/api/health-records/patient/{id}` | Patient records |
| POST | `/api/health-records` | Add health record |
| GET | `/api/health-records/patient/{id}/vitals` | Patient vitals |
| GET | `/api/notifications/user/{id}` | User notifications |
| POST | `/api/notifications/send` | Send notification |
| GET | `/api/admin/stats` | System statistics |
| GET | `/api/admin/users` | All users |
| DELETE | `/api/admin/users/{id}` | Delete user |

---

## Database Schema

16 tables, fully normalized to **3NF / BCNF**:

| Table | Purpose |
|---|---|
| `users` | Core user accounts |
| `patients` | Patient profiles |
| `doctors` | Doctor profiles |
| `admins` | Admin profiles |
| `roles` | Role lookup (PATIENT, DOCTOR, ADMIN) |
| `appointments` | Appointment bookings |
| `appointment_status` | Status lookup |
| `health_records` | Diagnoses & prescriptions |
| `vitals` | Patient vital signs |
| `notifications` | System notifications |
| `specializations` | Medical specializations |
| `doctor_specializations` | M:N doctor–specialization |
| `contacts` | Phone / email per user |
| `gender` | Gender lookup |
| `blood_group` | Blood group lookup |
| `contact_type` | Contact type lookup |

---

## Troubleshooting

| Problem | Solution |
|---|---|
| `JAVA_HOME not set` | Set `JAVA_HOME` to your JDK 22+ path |
| `Access denied for user` | Check `DB_PASSWORD` in `.env` matches MySQL |
| `Table not found` | Run `schema.sql` before `seed.sql` |
| `Port 8080 in use` | Change `SERVER_PORT` in `.env` |
| Lombok compilation error | Use JDK 22 for compilation |
| Swagger 500 error | Remove or disable `GlobalExceptionHandler.java` |

---

## Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Frontend | JavaFX, FXML, CSS | 26 |
| Backend | Spring Boot, JDBC | 3.4.1 |
| Database | MySQL | 8.0 |
| Security | SHA-256 + JWT | — |
| API Docs | Swagger / OpenAPI | 3.0 |
| Build | Maven | 3.9+ |
| Language | Java | 17+ |

---

# Health Assistance System — Installation Guide

## Prerequisites

1. **Java JDK 22+** — Download from https://jdk.java.net/
2. **MySQL 8** — Download from https://dev.mysql.com/downloads/
3. **JavaFX SDK 26** — Download from https://gluonhq.com/products/javafx/
   - Extract to `C:\javafx-sdk-26`

## Quick Setup

1. Extract this project folder to your Desktop
2. Double-click `setup.bat` — enter your MySQL root password
3. Double-click `launcher.bat` — system starts

## Manual Setup

### Database
Run these files in MySQL Workbench:
- `database/schema.sql`
- `database/seed.sql`

### Backend
```bash
cd backend
mvnw spring-boot:run

*Health Assistance System · Educational Project · © 2026*