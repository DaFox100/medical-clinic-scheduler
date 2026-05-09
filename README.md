# Medical Clinic Scheduling System

Spring Boot medical clinic scheduling software with MySQL persistence, server-rendered Thymeleaf pages, role-based login, provider slot management, appointment booking/rescheduling, mock confirmation notifications, and health/logging support.

## Tech Stack

- Java 17
- Spring Boot 4
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate
- MySQL
- Thymeleaf
- Maven

## Current Features

- Login with role-based access for `ADMIN`, `PROVIDER`, and `PATIENT`
- Home schedule board grouped by department and provider
- Department and date-range filtering on the home page
- Appointment booking with double-booking protection
- Appointment rescheduling
- Provider/admin time-slot management
- Provider-mode empty-slot creation from the home schedule
- Mock confirmation service call after successful booking
- `/health` endpoint with database/application status
- Structured logging with request correlation IDs

## Demo Accounts

All demo accounts use the password `password`.

- `admin`
- `provider.patel`
- `provider.nguyen`
- `provider.rivera`
- `patient.jane`
- `patient.bob`
- `patient.sam`

## Prerequisites

- Java 17+
- Maven
- Local MySQL server

## Database Setup

Create the database:

```sql
CREATE DATABASE medical_clinic;
```

Run the application with your MySQL credentials:

```bash
DB_USERNAME=root DB_PASSWORD=your_password mvn spring-boot:run
```

If needed, you can also override the full JDBC URL:

```bash
DB_URL='jdbc:mysql://localhost:3306/medical_clinic?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Los_Angeles' \
DB_USERNAME=root \
DB_PASSWORD=your_password \
mvn spring-boot:run
```

## Running the App

From the project root:

```bash
mvn spring-boot:run
```

Main routes:

- `/`
- `/login`
- `/appointments`
- `/appointments/book`
- `/time-slots/manage`
- `/health`

## Testing

Run tests with:

```bash
mvn test -q
```

## Project Structure

- `src/main/java/com/example/termproj_172/config`
  Security, logging, startup seeding, and HTTP client config
- `src/main/java/com/example/termproj_172/controllers`
  MVC controllers and web routes
- `src/main/java/com/example/termproj_172/domainModels`
  Entities, enums, DTOs, and form models
- `src/main/java/com/example/termproj_172/repositories`
  JPA repositories
- `src/main/java/com/example/termproj_172/services`
  Core business logic
- `src/main/java/com/example/termproj_172/viewmodels`
  Home schedule page view models
- `src/main/resources/templates`
  Thymeleaf templates
- `src/main/resources/static/css`
  Stylesheet assets
- `docs/project-file-guide.md`
  Detailed description of classes and files

## Notes

- Database interaction is done through Spring Data JPA and Hibernate, which use JDBC underneath.
- Demo data and demo accounts are created by `DataSeeder` on startup if they are missing.
- The project currently uses `spring.jpa.hibernate.ddl-auto=update` for development convenience.
