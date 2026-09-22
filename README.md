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
- Durable asynchronous confirmation delivery with retry backoff
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

The default connection uses local MySQL on port 3306, database `medical_clinic`,
and user `root` with an empty password, matching the local development setup.
If your MySQL account has a password, set `DB_PASSWORD` as shown below.
Appointments and scheduling data are stored in MySQL and survive application
restarts; Flyway migrations manage schema changes and Hibernate validates entity mappings.

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
- Flyway manages schema versions; Hibernate uses `ddl-auto=validate`.

## Backend verification and reliable notifications

Booking commits an appointment and an immutable notification snapshot in the same
MySQL transaction. A scheduled worker delivers committed events over HTTP, with
2-second connect and 3-second read timeouts. Failed deliveries retry with exponential
backoff (5 seconds initially, capped at one hour). Pending events survive app restarts.
Workers lock one event at a time with `FOR UPDATE SKIP LOCKED`; HTTP happens in a
separate worker transaction, never in the booking transaction.

Delivery is **at least once**, not exactly once: a crash after remote acceptance but
before marking an event sent can cause a retry. The same `Idempotency-Key` is sent on
every attempt. The mock returns a stable message ID for that key; a real email/SMS
receiver must durably deduplicate keys before performing external side effects.
The mock still runs in the same app and does not send real email.

### Existing database upgrade

For the original Hibernate-created `medical_clinic` database, take a backup and run
once with `DB_BASELINE=true mvn spring-boot:run`. This explicitly baselines version 0,
then applies V1 (existing clinic tables are retained) and V2 (new outbox table).
Subsequent starts use `mvn spring-boot:run` normally. Fresh databases need no baseline.
Do not enable baselining for an unrelated database. Migration checksums are verified
on subsequent starts; add new migrations instead of editing applied ones.

### Tests

`./mvnw test` runs unit tests without a database.

`./mvnw -Pintegration verify` also runs integration tests against local MySQL using
**medical_clinic_test**. This dedicated database is cleared between tests. The suite
rejects URLs naming other databases. Override credentials with `TEST_DB_USERNAME`
and `TEST_DB_PASSWORD`; optionally set `TEST_DB_URL` to a MySQL JDBC URL pointing
to `medical_clinic_test`. Existing clinic data is not used.

The integration suite verifies:
- 20 simultaneous service-layer booking attempts per slot over 10 rounds;
- exactly one persisted appointment and outbox event per winning booking;
- transaction rollback removes the appointment/event and releases the slot;
- HTTP 503 notification failure preserves booking, delays retry, and recovers.

Results are written to `target/concurrency-results.json`. These measure service-layer
contention against MySQL, not HTTP throughput or production traffic.

The GitHub Actions workflow runs unit/integration tests on Java 17 with MySQL 8.4
and uploads test reports and concurrency results. It runs when this repository is
pushed to GitHub or a pull request is opened.
