# HUPMS Backend

Spring Boot REST API for the Hajj & Umrah Pilgrim Management System PRD.

## Requirements

- Java 17
- Maven
- PostgreSQL database named `hupms`

Set `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` as environment variables, or update the defaults in
`src/main/resources/application.properties` for local demos.

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

Postman collection and environment files are available in `docs/postman/`.

1. `POST /api/auth/register` with role `ADMIN`
2. `POST /api/auth/login` and copy the Bearer token
3. `POST /api/packages` with the admin token
4. `POST /api/auth/register` with role `AGENT`
5. Login as agent
6. `POST /api/groups` with the agent token, or with an admin token and an `agentId`
7. `POST /api/pilgrims` with the agent token; include `password` if the pilgrim should log in directly
8. `PATCH /api/pilgrims/{id}/status`
9. Call an admin endpoint with the agent token to see `403`
10. `GET /api/audit` with the admin token
