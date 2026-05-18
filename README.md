# HUPMS Backend

Spring Boot REST API for the Hajj & Umrah Pilgrim Management System PRD.

## Requirements

- Java 17
- Maven
- PostgreSQL database named `hupms`

Update `src/main/resources/application.properties` with your PostgreSQL password before running.

## Run

```bash
mvn spring-boot:run
```

## Verify

```bash
mvn test
mvn package -DskipTests
```

## Demo Flow

1. `POST /api/auth/register` with role `ADMIN`
2. `POST /api/auth/login` and copy the Bearer token
3. `POST /api/packages` with the admin token
4. `POST /api/auth/register` with role `AGENT`
5. Login as agent
6. `POST /api/groups` with the agent token
7. `POST /api/pilgrims` with the agent token
8. `PATCH /api/pilgrims/{id}/status`
9. Call an admin endpoint with the agent token to see `403`
10. `GET /api/audit` with the admin token
