# 📋 Health Assistance System — API Documentation

> **Base URL:** `http://localhost:8080/api`  
> **Swagger UI:** `http://localhost:8080/swagger-ui.html`  
> **OpenAPI Spec:** `http://localhost:8080/api-docs`

---

## 🔐 Authentication

### POST `/api/auth/login`
Login with email and password. Returns JWT token.

**Request:**
```json
{
  "email": "rahel.asmamaw@gmail.com",
  "password": "Rahel@2024!"
}

