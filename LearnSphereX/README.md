# LearnSphereX — Enterprise Learning & Assessment Management System

LearnSphereX is the consolidated modular-monolith implementation of the supplied project modules, renamed consistently across the backend, Thymeleaf frontend, configuration, Maven metadata, Eclipse project metadata, and database bootstrap.

## What is included

- User & role management: ADMIN, TRAINER, STUDENT, HR, EVALUATOR
- Registration, login/logout, password reset, account status and audit support
- Course → modules → topics → materials
- Course technologies
- Batch management, trainer assignment, enrollment, schedules, sessions, holidays and attendance
- Student management and student dashboard
- Assignments, submissions, version/evaluation support
- Online examination engine: MCQ/descriptive/coding/project question structures, attempts, answers, timing/score data and MCQ evaluation
- Project management, evaluation criteria, submissions and evaluations
- Payment, fee plans, installments and receipts
- Certificates and eligibility checks
- Notifications, Spring events, mail integration and scheduling
- Reporting and admin dashboard
- Audit logging
- Spring Security + JWT
- Thymeleaf web UI
- Actuator health/metrics
- MySQL database bootstrap and optional Docker Compose MySQL

## Architecture

This follows the project description's modular-monolith approach rather than splitting the application into unrelated controller/service/repository packages.

```text
com.learnspherex
├── auth
├── security
├── student
├── trainer
├── course
├── batch
├── assignment
├── examination
├── project
├── payment
├── certificate
├── notification
├── reporting
├── audit
├── exception
├── common
└── web
```

Technology stack:

- Java 21
- Spring Boot 3.5.5
- Spring MVC
- Spring Security
- Spring Data JPA
- Spring Validation
- Spring Mail
- Thymeleaf
- MySQL
- JWT
- Lombok
- Actuator
- OpenPDF

## Database

Default database:

`learnspherex_db`

You can create it with:

```sql
CREATE DATABASE IF NOT EXISTS learnspherex_db;
```

or run:

`database/01_create_database.sql`

The application is configured with Hibernate `ddl-auto=update`, so the JPA entities create/update the tables when the application starts.

For Docker MySQL:

```bash
docker compose up -d
```

If your MySQL username/password are different, set:

- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_URL`

You can also change these in:

`src/main/resources/application.properties`

## Run in Eclipse / Spring Tool Suite

1. Extract the ZIP.
2. Open Eclipse/STS.
3. Choose **File → Import → Existing Maven Projects**.
4. Select the folder containing `pom.xml`.
5. Ensure JDK 21 is configured.
6. Create/start MySQL, or run `docker compose up -d`.
7. Verify the database settings in `application.properties`.
8. Run `LearnSphereXApplication.java` as **Spring Boot App**.
9. Open `http://localhost:8082`.

Health endpoint:

`http://localhost:8082/actuator/health`

## Maven command

Windows:

```bat
mvnw.cmd clean test
mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw clean test
./mvnw spring-boot:run
```

## Development admin

The supplied development role seeder creates:

- Username: `admin`
- Password: `Admin@123`

Change/remove this development account before production deployment.

## Important verification note

The four supplied ZIPs contained overlapping/variant implementations. The consolidated source uses the Enterprise project as the primary backend, incorporates the additional course/assignment web UI and course technology implementation from the other Enterprise variant, and adds the useful global exception/catalog repository files from the Integrated variant. Duplicate build output and Eclipse workspace metadata were not merged.

A full Maven build could not be executed in this environment because the Maven wrapper attempted to download Maven/dependencies from Maven Central and external dependency download was unavailable. Therefore this deliverable has been statically consolidated and consistency-checked, but you should run `mvnw.cmd clean test` once in your Eclipse/Internet-enabled environment.

## Project description coverage

The supplied project description calls for the enterprise modules covering users/roles, courses, batches, students, attendance, assignments, examinations, project evaluation, certification, payments, notifications, reporting, audit logging, exception handling, Spring Boot/Security/Data JPA/Validation/MVC/Mail, Thymeleaf and MySQL. The consolidated source contains these corresponding module packages and web pages.

## Final application name

All former project identifiers in the consolidated source have been renamed to `LearnSphereX` / `learnspherex`, including:

- Maven group/artifact metadata
- Spring application name
- Java package namespace
- Main application class
- Eclipse project metadata
- Thymeleaf branding
- JWT/default database identifiers
- Database name
- README and database documentation
