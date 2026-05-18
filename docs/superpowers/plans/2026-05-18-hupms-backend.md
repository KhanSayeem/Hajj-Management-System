# HUPMS Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the Hajj & Umrah Pilgrim Management System backend REST API from the PRD.

**Architecture:** A Spring Boot 3 application using controllers, services, Spring JDBC repositories, JWT security, DTO validation, and centralized exception handling. Business rules live in services; repositories are SQL-only.

**Tech Stack:** Java 17, Maven, Spring Boot 3.x, Spring Security, Spring JDBC, PostgreSQL, JJWT, JUnit 5.

---

### Task 1: Project Scaffold And Domain Tests

**Files:**
- Create: `pom.xml`
- Create: `src/test/java/com/hupms/service/PilgrimServiceTest.java`

- [x] **Step 1: Add Maven scaffold configured for Java 17**

Create a Spring Boot Maven project with web, security, JDBC, validation, PostgreSQL, JJWT, and test dependencies.

- [x] **Step 2: Write failing tests for core business rules**

Cover group capacity, status transition validation, and agent ownership checks in service tests.

- [x] **Step 3: Run tests to verify RED**

Run: `mvn test`
Expected: compilation fails because production classes do not exist yet.

### Task 2: Models, DTOs, Exceptions, And Repositories

**Files:**
- Create: `src/main/java/com/hupms/model/**`
- Create: `src/main/java/com/hupms/dto/**`
- Create: `src/main/java/com/hupms/enums/**`
- Create: `src/main/java/com/hupms/exception/**`
- Create: `src/main/java/com/hupms/repository/**`
- Create: `src/main/resources/schema.sql`

- [x] **Step 1: Implement POJO models and enums**

Add `BaseEntity`, users, packages, groups, pilgrims, audit logs, and enum types.

- [x] **Step 2: Implement DTOs and API wrapper**

Add request/response DTOs with Jakarta Bean Validation annotations and `ApiResponse<T>`.

- [x] **Step 3: Implement Spring JDBC repositories**

Use `JdbcTemplate`, generated keys, `RowMapper<T>`, count/exists helpers, and PostgreSQL schema.

### Task 3: Services And Security

**Files:**
- Create: `src/main/java/com/hupms/service/**`
- Create: `src/main/java/com/hupms/security/**`
- Create: `src/main/java/com/hupms/config/**`

- [x] **Step 1: Implement services**

Add auth, package, group, pilgrim, and audit services. Enforce transactionality, capacity, ownership, delete guards, and status transition rules.

- [x] **Step 2: Implement JWT security**

Add stateless Spring Security configuration, BCrypt password encoding, JWT provider/filter, and user details service.

- [x] **Step 3: Run tests to verify GREEN**

Run: `mvn test`
Expected: service tests pass.

### Task 4: REST Controllers And Runtime Config

**Files:**
- Create: `src/main/java/com/hupms/controller/**`
- Create: `src/main/resources/application.properties`
- Create: `README.md`

- [x] **Step 1: Implement controllers**

Expose auth, package, group, pilgrim, and audit routes with role annotations and `ApiResponse<T>`.

- [x] **Step 2: Add properties and README demo flow**

Configure port, PostgreSQL datasource placeholders, SQL init, JWT, and usage notes.

- [x] **Step 3: Verify package**

Run: `mvn test` and `mvn package -DskipTests`.
Expected: both commands exit 0.
