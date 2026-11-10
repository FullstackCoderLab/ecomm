# eCommUserAuth - E-Commerce REST API

A modern, production-ready Spring Boot 3 RESTful web service for E-Commerce user authentication and management with **H2 In-Memory Database**, **Spring Security 6**, **JWT (JSON Web Token)**, and **Swagger / OpenAPI 3**.

---

## 🌟 Key Features

1. **User Registration**:
   - Accepts user details (`name`, `email`, `password`, optional `role`).
   - Validates inputs using Jakarta Bean Validation (`@Valid`, `@Email`, `@NotBlank`, `@Size`).
   - Checks for duplicate emails and stores encrypted passwords with BCrypt.
   - Automatically issues a JWT upon successful registration.

2. **User Login**:
   - Authenticates credentials (`email` + `password`) against database via Spring Security.
   - Issues signed HMAC-SHA256 JWT tokens valid for 24 hours.

3. **JWT Authentication & Authorization**:
   - Stateless security with `JwtAuthenticationFilter` and `SecurityFilterChain`.
   - Role-Based Access Control (RBAC) with `ROLE_USER` and `ROLE_ADMIN` (`@PreAuthorize`).
   - Custom `JwtAuthEntryPoint` returning structured JSON 401 Unauthorized responses.

4. **H2 In-Memory Database**:
   - Zero-installation embedded database running inside the JVM.
   - Spring Data JPA with Hibernate ORM (`ddl-auto=update`).
   - Dedicated `users` table with unique constraint on `email`.
   - Built-in **H2 Web Console** for browser-based database querying.

5. **Interactive Swagger / OpenAPI 3**:
   - Complete documentation of all endpoints and DTO schemas with SpringDoc OpenAPI.
   - Built-in **"Authorize"** button in Swagger UI to test protected endpoints using Bearer JWT tokens.

---

## 📁 Project Structure

```
eComm/
├── pom.xml                                       # Maven build file with Spring Boot 3, JJWT, H2 & Swagger
├── .project                                      # STS / Eclipse project configuration
├── .classpath                                    # Eclipse classpath definition
├── .settings/                                    # Java 21 & UTF-8 IDE compiler preferences
├── src/
│   ├── main/
│   │   ├── java/com/ecomm/
│   │   │   ├── ECommApplication.java            # Spring Boot main entry point
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java           # Swagger UI + Bearer JWT security scheme
│   │   │   │   └── SecurityConfig.java          # Spring Security 6 filter chain, H2 console & BCrypt
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java          # Public /register & /login endpoints
│   │   │   │   └── UserController.java          # Protected /me and admin endpoints
│   │   │   ├── dto/
│   │   │   │   ├── ApiResponse.java             # Generic JSON response wrapper
│   │   │   │   ├── AuthResponse.java            # JWT access token payload
│   │   │   │   ├── LoginRequest.java            # Email & password login payload
│   │   │   │   ├── RegisterRequest.java         # Name, email, password registration payload
│   │   │   │   └── UserProfileResponse.java     # User profile response
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java  # @RestControllerAdvice for validation & errors
│   │   │   │   └── UserAlreadyExistsException.java
│   │   │   ├── model/
│   │   │   │   ├── Role.java                    # ROLE_USER, ROLE_ADMIN enum
│   │   │   │   └── User.java                    # JPA Entity mapped to 'users' table
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java          # Spring Data JPA repository
│   │   │   ├── security/
│   │   │   │   ├── CustomUserDetails.java       # UserDetails adapter
│   │   │   │   ├── CustomUserDetailsService.java# Loads user by email from database
│   │   │   │   ├── JwtAuthEntryPoint.java       # JSON 401 unauthorized handler
│   │   │   │   ├── JwtAuthenticationFilter.java # OncePerRequestFilter for Bearer JWT
│   │   │   │   └── JwtUtils.java                # Token generation, parsing, and validation
│   │   │   └── service/
│   │   │       ├── AuthService.java             # Registration & login business logic
│   │   │       └── UserService.java             # User profile & management queries
│   │   └── resources/
│   │       └── application.properties           # H2 Database, JWT, server port & Swagger configs
│   └── test/
│       └── java/com/ecomm/
│           └── ECommApplicationTests.java
└── README.md
```

---

## 🗄️ H2 Database & Web Console

H2 runs embedded in memory, meaning **no external database installation is required**.

When the application is running:
- **H2 Web Console URL**: [http://localhost:8081/h2-console](http://localhost:8081/h2-console)
- **JDBC URL**: `jdbc:h2:mem:ecommdb`
- **User Name**: `sa`
- **Password**: *(leave blank)*

Click **Connect** to open the SQL console and inspect the `USERS` table directly.

---

## 🚀 How to Run in Spring Tool Suite (STS 4)

1. Open **Spring Tool Suite (STS)**.
2. Go to **File** > **Import...**
3. Expand **Maven** > Select **Existing Maven Projects** > Click **Next**.
4. In **Root Directory**, click **Browse...** and choose:
   `C:\Users\NADEEM\STS_NoorAI\eComm`
5. Ensure `pom.xml` is checked, then click **Finish**.
6. **Force Maven Update**:
   - Right-click `eComm` in Package Explorer > **Maven** > **Update Project...** (`Alt + F5`) > check **Force Update of Snapshots/Releases** > Click **OK**.
7. **Run the Application**:
   - Right-click `eComm` > **Run As** > **Spring Boot App**.
   - The application will start on **`http://localhost:8081`** (avoiding conflict with port 8080).

---

## 📖 Swagger / OpenAPI Interactive Documentation

- **Swagger UI**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **OpenAPI 3 JSON Spec**: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

### How to Authenticate in Swagger UI:
1. Open [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html).
2. Expand the **Authentication** section and execute `POST /api/v1/auth/login` (or `/register`).
3. Copy the `accessToken` value from the response body.
4. Click the green **Authorize 🔓** button at the top right of the Swagger UI page.
5. Paste the token into the **Value** box and click **Authorize**.
6. Now you can execute protected endpoints (like `GET /api/v1/users/me`) directly from Swagger UI!

---

## 🌐 REST API Endpoints Overview

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Register new user; returns JWT token & user info |
| `POST` | `/api/v1/auth/login` | Public | Authenticate user credentials; returns JWT token |
| `GET` | `/api/v1/users/me` | Authenticated (JWT) | Get current authenticated user's profile |
| `GET` | `/api/v1/users` | Admin (`ROLE_ADMIN`) | Get list of all registered users |

---

## 🧪 Testing with PowerShell / cURL

### 1. Register a New User
```powershell
$registerBody = @{
    name = "John Doe"
    email = "john.doe@example.com"
    password = "SecurePassword123!"
    role = "ROLE_USER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/v1/auth/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $registerBody
```

### 2. Login to Obtain JWT Token
```powershell
$loginBody = @{
    email = "john.doe@example.com"
    password = "SecurePassword123!"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body $loginBody

# Print JWT Token
$token = $loginResponse.data.accessToken
Write-Host "JWT Token: $token"
```

### 3. Access Protected Endpoint (`/api/v1/users/me`) with Bearer Token
```powershell
$headers = @{
    Authorization = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8081/api/v1/users/me" `
    -Method Get `
    -Headers $headers
```
