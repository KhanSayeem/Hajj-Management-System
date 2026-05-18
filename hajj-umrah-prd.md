# Product Requirements Document
# Hajj & Umrah Pilgrim Management System (HUPMS)

**Version:** 1.0  
**Type:** Backend REST API  
**Stack:** Java 17 · Spring Boot 3.x · Spring Security · Spring JDBC · PostgreSQL · Maven  
**Auth:** JWT (Bearer Token)  
**Client:** Postman (no frontend)  
**Purpose:** Academic project submission — fulfils Java OOP, Spring Framework, Security, and Version Control criteria

---

## 1. Project Overview

HUPMS is a lightweight REST API backend that allows Hajj/Umrah travel agencies to manage pilgrim registrations, group assignments, agent accounts, and journey status tracking. The system is intentionally scoped to core operations only — no payment processing, no visa integration, no UI.

---

## 2. Actors / Roles

| Role | Description |
|------|-------------|
| `ADMIN` | Full system access. Manages agents, packages, and all data. |
| `AGENT` | Manages their own pilgrims and groups. Cannot access other agents' data. |
| `PILGRIM` | Read-only. Can view their own profile and group info. |

---

## 3. Database Entities (Minimum 5 Required)

### 3.1 `users`
Stores all system users (admins, agents, pilgrims).

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGSERIAL PK | |
| `full_name` | VARCHAR(100) | NOT NULL |
| `email` | VARCHAR(150) | UNIQUE, NOT NULL |
| `password_hash` | VARCHAR(255) | BCrypt hashed |
| `role` | VARCHAR(20) | ENUM: ADMIN, AGENT, PILGRIM |
| `is_active` | BOOLEAN | Default true |
| `created_at` | TIMESTAMP | Default NOW() |

---

### 3.2 `packages`
Represents a Hajj or Umrah travel package offered by the agency.

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGSERIAL PK | |
| `name` | VARCHAR(100) | e.g. "Economy Umrah 2025" |
| `type` | VARCHAR(10) | ENUM: HAJJ, UMRAH |
| `year` | INT | e.g. 2025 |
| `capacity` | INT | Max pilgrims |
| `price_usd` | DECIMAL(10,2) | |
| `departure_date` | DATE | |
| `return_date` | DATE | |
| `created_by` | BIGINT FK → users.id | Admin who created it |
| `created_at` | TIMESTAMP | Default NOW() |

---

### 3.3 `groups`
A group is a batch of pilgrims under a package, managed by an agent.

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGSERIAL PK | |
| `group_name` | VARCHAR(100) | e.g. "Group A - Dhaka North" |
| `package_id` | BIGINT FK → packages.id | NOT NULL |
| `agent_id` | BIGINT FK → users.id | Agent managing this group |
| `max_size` | INT | Max pilgrims in group |
| `created_at` | TIMESTAMP | Default NOW() |

---

### 3.4 `pilgrims`
Pilgrim profile with personal and journey details.

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGSERIAL PK | |
| `user_id` | BIGINT FK → users.id | UNIQUE (one profile per user) |
| `group_id` | BIGINT FK → groups.id | NULLABLE (can be unassigned) |
| `passport_number` | VARCHAR(50) | UNIQUE, NOT NULL |
| `date_of_birth` | DATE | |
| `nationality` | VARCHAR(60) | |
| `phone` | VARCHAR(20) | |
| `gender` | VARCHAR(10) | ENUM: MALE, FEMALE |
| `mahram_id` | BIGINT FK → pilgrims.id | NULLABLE — for female pilgrims requiring a mahram |
| `status` | VARCHAR(20) | ENUM: REGISTERED, APPROVED, DEPARTED, IN_MAKKAH, RETURNED, CANCELLED |
| `registered_at` | TIMESTAMP | Default NOW() |
| `updated_at` | TIMESTAMP | Auto-update on change |

---

### 3.5 `audit_logs`
Tracks all significant state changes in the system (satisfies I/O + exception handling criterion).

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGSERIAL PK | |
| `actor_id` | BIGINT FK → users.id | Who performed the action |
| `action` | VARCHAR(100) | e.g. "PILGRIM_STATUS_CHANGED", "GROUP_CREATED" |
| `entity_type` | VARCHAR(50) | e.g. "Pilgrim", "Group" |
| `entity_id` | BIGINT | ID of affected record |
| `details` | TEXT | JSON string with before/after or extra context |
| `performed_at` | TIMESTAMP | Default NOW() |

---

## 4. Java & OOP Requirements

### 4.1 Class Design
- Each entity maps to a POJO/model class with private fields and public getters/setters (encapsulation)
- `BaseEntity` abstract class with `id`, `createdAt` fields — extended by all models (inheritance)
- `AuditableService` interface with `log(action, entityType, entityId, details)` — implemented by services that write audit logs (interfaces)

### 4.2 Collections, Generics, Enums, Streams
- Enums: `Role`, `PilgrimStatus`, `PackageType`, `Gender`
- Generic response wrapper: `ApiResponse<T>` used across all endpoints
- Java Streams used in service layer for filtering, mapping, and aggregating lists (e.g. filtering available groups by package capacity)

### 4.3 Exception Handling & I/O
- Custom exceptions: `ResourceNotFoundException`, `UnauthorizedAccessException`, `DuplicatePassportException`, `GroupCapacityExceededException`
- Global `@ControllerAdvice` handler maps exceptions to structured JSON error responses
- `try-with-resources` used wherever `InputStream`/`OutputStream` or JDBC resources are opened manually

---

## 5. REST API Endpoints

### 5.1 Auth (`/api/auth`)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | Public | Register a new agent or pilgrim account |
| POST | `/api/auth/login` | Public | Login and receive JWT token |
| GET | `/api/auth/me` | Any authenticated | Get current user profile |

**POST /api/auth/register — Request Body:**
```json
{
  "fullName": "Ahmed Hossain",
  "email": "ahmed@example.com",
  "password": "securepass123",
  "role": "AGENT"
}
```

**POST /api/auth/login — Request Body:**
```json
{
  "email": "ahmed@example.com",
  "password": "securepass123"
}
```

**POST /api/auth/login — Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "fullName": "Ahmed Hossain",
    "email": "ahmed@example.com",
    "role": "AGENT"
  }
}
```

---

### 5.2 Packages (`/api/packages`)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/packages` | ADMIN | Create a new package |
| GET | `/api/packages` | ADMIN, AGENT | List all packages |
| GET | `/api/packages/{id}` | ADMIN, AGENT | Get package by ID |
| PUT | `/api/packages/{id}` | ADMIN | Update package details |
| DELETE | `/api/packages/{id}` | ADMIN | Delete package (only if no groups attached) |

---

### 5.3 Groups (`/api/groups`)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/groups` | ADMIN, AGENT | Create a group under a package |
| GET | `/api/groups` | ADMIN | List all groups |
| GET | `/api/groups/my` | AGENT | List groups managed by current agent |
| GET | `/api/groups/{id}` | ADMIN, AGENT | Get group details with pilgrim list |
| PUT | `/api/groups/{id}` | ADMIN, AGENT (owner) | Update group name or size |
| DELETE | `/api/groups/{id}` | ADMIN | Delete group (only if no pilgrims assigned) |

---

### 5.4 Pilgrims (`/api/pilgrims`)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/pilgrims` | AGENT | Register a new pilgrim (creates user + pilgrim profile atomically) |
| GET | `/api/pilgrims` | ADMIN | List all pilgrims |
| GET | `/api/pilgrims/my` | AGENT | List pilgrims in agent's groups |
| GET | `/api/pilgrims/{id}` | ADMIN, AGENT (owner), PILGRIM (self) | Get pilgrim profile |
| PUT | `/api/pilgrims/{id}` | AGENT (owner) | Update pilgrim personal details |
| PATCH | `/api/pilgrims/{id}/status` | ADMIN, AGENT (owner) | Update pilgrim journey status |
| PATCH | `/api/pilgrims/{id}/assign-group` | AGENT (owner) | Assign pilgrim to a group |
| DELETE | `/api/pilgrims/{id}` | ADMIN | Remove pilgrim |

**POST /api/pilgrims — Request Body:**
```json
{
  "fullName": "Fatima Begum",
  "email": "fatima@example.com",
  "passportNumber": "BD1234567",
  "dateOfBirth": "1980-05-15",
  "nationality": "Bangladeshi",
  "phone": "+8801711000000",
  "gender": "FEMALE",
  "groupId": 2,
  "mahramId": null
}
```

**PATCH /api/pilgrims/{id}/status — Request Body:**
```json
{
  "status": "DEPARTED"
}
```

---

### 5.5 Audit Logs (`/api/audit`)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/audit` | ADMIN | List all audit logs (paginated) |
| GET | `/api/audit?entityType=Pilgrim&entityId=5` | ADMIN | Filter logs by entity |

---

### 5.6 Standard Response Wrapper

All endpoints return:
```json
{
  "success": true,
  "message": "Pilgrim registered successfully",
  "data": { ... },
  "timestamp": "2025-05-18T09:00:00Z"
}
```

Error responses:
```json
{
  "success": false,
  "message": "Passport number already exists",
  "error": "DuplicatePassportException",
  "timestamp": "2025-05-18T09:00:00Z"
}
```

---

## 6. Spring Framework Requirements

### 6.1 Spring Boot & Maven Structure

```
hupms/
├── pom.xml
└── src/
    └── main/
        ├── java/com/hupms/
        │   ├── HupmsApplication.java
        │   ├── config/
        │   │   ├── SecurityConfig.java
        │   │   └── JwtConfig.java
        │   ├── controller/
        │   │   ├── AuthController.java
        │   │   ├── PackageController.java
        │   │   ├── GroupController.java
        │   │   ├── PilgrimController.java
        │   │   └── AuditController.java
        │   ├── service/
        │   │   ├── AuthService.java
        │   │   ├── PackageService.java
        │   │   ├── GroupService.java
        │   │   ├── PilgrimService.java
        │   │   └── AuditService.java
        │   ├── repository/
        │   │   ├── UserRepository.java
        │   │   ├── PackageRepository.java
        │   │   ├── GroupRepository.java
        │   │   ├── PilgrimRepository.java
        │   │   └── AuditLogRepository.java
        │   ├── model/
        │   │   ├── BaseEntity.java
        │   │   ├── User.java
        │   │   ├── Package.java
        │   │   ├── Group.java
        │   │   ├── Pilgrim.java
        │   │   └── AuditLog.java
        │   ├── dto/
        │   │   ├── request/
        │   │   └── response/
        │   ├── enums/
        │   │   ├── Role.java
        │   │   ├── PilgrimStatus.java
        │   │   ├── PackageType.java
        │   │   └── Gender.java
        │   ├── exception/
        │   │   ├── ResourceNotFoundException.java
        │   │   ├── UnauthorizedAccessException.java
        │   │   ├── DuplicatePassportException.java
        │   │   ├── GroupCapacityExceededException.java
        │   │   └── GlobalExceptionHandler.java
        │   ├── security/
        │   │   ├── JwtTokenProvider.java
        │   │   ├── JwtAuthenticationFilter.java
        │   │   └── CustomUserDetailsService.java
        │   └── util/
        │       └── ApiResponse.java
        └── resources/
            ├── application.properties
            └── schema.sql
```

---

### 6.2 REST API & Validation

- Use `@Valid` and Jakarta Bean Validation annotations on all DTOs
- Annotations: `@NotBlank`, `@Email`, `@Size`, `@NotNull`, `@Past` (for date of birth)
- `@RestController`, `@RequestMapping`, `@PathVariable`, `@RequestBody`, `@RequestParam`

---

### 6.3 Database — Spring JDBC

- Use `JdbcTemplate` and `NamedParameterJdbcTemplate` — **no JPA/Hibernate**
- `RowMapper<T>` implementations for each entity
- All DDL in `schema.sql`, auto-run on startup via `spring.sql.init.mode=always`
- Transactions via `@Transactional` — critical on `PilgrimService.registerPilgrim()` which must atomically:
  1. INSERT into `users`
  2. INSERT into `pilgrims`
  3. INSERT into `audit_logs`
  — all rolled back if any step fails

---

### 6.4 Spring Security & JWT

- Stateless session (`SessionCreationPolicy.STATELESS`)
- JWT signed with HS256, expiry: 24 hours
- `JwtAuthenticationFilter` extends `OncePerRequestFilter` — validates token on every request
- Public endpoints: `POST /api/auth/register`, `POST /api/auth/login`
- All other endpoints require valid Bearer token
- Method-level security with `@PreAuthorize("hasRole('ADMIN')")` where needed
- Password encoding: BCryptPasswordEncoder

---

### 6.5 Layered Architecture

| Layer | Responsibility |
|-------|---------------|
| Controller | HTTP in/out, delegates to service, never touches repository directly |
| Service | Business logic, validation, orchestration, transaction management |
| Repository | SQL via JdbcTemplate only, no business logic |
| Model | POJOs, extends BaseEntity |
| DTO | Request/response shapes, never expose model directly to controller |
| Security | Filter chain, JWT, UserDetails |
| Exception | Custom exceptions + GlobalExceptionHandler |

---

## 7. Key Business Rules

1. A pilgrim can only belong to one group at a time
2. A group cannot exceed its `max_size` — throw `GroupCapacityExceededException` if attempted
3. A female pilgrim (`gender = FEMALE`) may optionally reference a `mahram_id` pointing to a male pilgrim in the same group
4. A package cannot be deleted if it has groups attached
5. A group cannot be deleted if it has pilgrims assigned
6. An agent can only view/edit pilgrims and groups they manage — enforce in service layer
7. Status transitions must be logical: `REGISTERED → APPROVED → DEPARTED → IN_MAKKAH → RETURNED` (CANCELLED allowed from any state)
8. Every status change must create an `audit_log` entry
9. Pilgrim registration (`POST /api/pilgrims`) must be a single `@Transactional` operation

---

## 8. Maven Dependencies (`pom.xml`)

```xml
<dependencies>
  <!-- Spring Boot -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>

  <!-- Spring Security -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
  </dependency>

  <!-- Spring JDBC (NO JPA) -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
  </dependency>

  <!-- Validation -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>

  <!-- PostgreSQL Driver -->
  <dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
  </dependency>

  <!-- JWT -->
  <dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
  </dependency>
  <dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
  </dependency>

  <!-- Lombok (optional but recommended) -->
  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
  </dependency>

  <!-- Test -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

---

## 9. `application.properties`

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/hupms
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# SQL Init
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql

# JWT
app.jwt.secret=your-256-bit-secret-key-here-make-it-long-enough
app.jwt.expiration-ms=86400000

# Logging
logging.level.org.springframework.security=DEBUG
logging.level.com.hupms=DEBUG
```

---

## 10. `schema.sql`

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS packages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(10) NOT NULL,
    year INT NOT NULL,
    capacity INT NOT NULL,
    price_usd DECIMAL(10,2) NOT NULL,
    departure_date DATE,
    return_date DATE,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS groups (
    id BIGSERIAL PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL,
    package_id BIGINT NOT NULL REFERENCES packages(id),
    agent_id BIGINT NOT NULL REFERENCES users(id),
    max_size INT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pilgrims (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id),
    group_id BIGINT REFERENCES groups(id),
    passport_number VARCHAR(50) UNIQUE NOT NULL,
    date_of_birth DATE,
    nationality VARCHAR(60),
    phone VARCHAR(20),
    gender VARCHAR(10) NOT NULL,
    mahram_id BIGINT REFERENCES pilgrims(id),
    status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED',
    registered_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    details TEXT,
    performed_at TIMESTAMP DEFAULT NOW()
);
```

---

## 11. OOP Criteria Mapping

| Criterion | Implementation |
|-----------|---------------|
| Encapsulation | All model fields private, accessed via getters/setters |
| Inheritance | `BaseEntity` (abstract) → `User`, `Package`, `Group`, `Pilgrim`, `AuditLog` |
| Interfaces | `AuditableService` interface implemented by `PilgrimService`, `GroupService` |
| Collections | `List<Pilgrim>`, `List<Group>` returned from repositories |
| Generics | `ApiResponse<T>` wrapper, `RowMapper<T>` implementations |
| Enums | `Role`, `PilgrimStatus`, `PackageType`, `Gender` |
| Streams | Filter/map pilgrim lists in service layer (e.g. count approved pilgrims in a group) |
| Exception Handling | Custom exceptions + `@ControllerAdvice` global handler |
| try-with-resources | Used in any manual JDBC or file I/O operations |

---

## 12. Version Control Requirements

- Initialize Git repo at project root
- Commit after each major feature:
  - `feat: add user authentication with JWT`
  - `feat: add package and group management endpoints`
  - `feat: add pilgrim registration with transaction`
  - `feat: add audit logging`
  - `feat: implement spring security role-based access`
- Push to GitHub (public or private repo)
- Minimum 8–10 meaningful commits — no single giant commit

---

## 13. Demo Flow (Postman)

1. **Register admin** — `POST /api/auth/register` (role: ADMIN)
2. **Login as admin** — `POST /api/auth/login` → copy JWT
3. **Create a package** — `POST /api/packages` (with admin token)
4. **Register an agent** — `POST /api/auth/register` (role: AGENT)
5. **Login as agent** — get agent JWT
6. **Create a group** — `POST /api/groups` (agent token, link to package)
7. **Register a pilgrim** — `POST /api/pilgrims` (agent token, link to group)
8. **Update status** — `PATCH /api/pilgrims/1/status` → `APPROVED`
9. **Show unauthorized** — call admin endpoint with agent token → 403
10. **View audit log** — `GET /api/audit` (admin token) → shows all changes

---

## 14. Out of Scope

- No frontend or UI of any kind
- No email/SMS notifications
- No payment processing
- No visa integration
- No file uploads or document storage
- No pagination required (optional bonus if time allows)

---

*End of PRD — HUPMS v1.0*
