# Backend verification — September 22, 2026

## Measured concurrency result

Command: `mvn -q -Pintegration verify`
Environment: local macOS, Java 23 running Java 17-compatible code, MySQL 9.6.
CI is configured for Java 17 and MySQL 8.4; GitHub execution has not yet been observed.

| Measure | Result |
|---|---:|
| Rounds / separate slots | 10 |
| Simultaneous callers per round | 20 |
| Total booking attempts | 200 |
| Successful committed bookings | 10 |
| Expected conflict rejections | 190 |
| Duplicate appointments | 0 |
| Persisted notification events in contention test | 10 |

Each round starts callers with a barrier, invokes the real Spring transactional
booking service, and checks MySQL rows after every result has completed. This is a
service-layer contention test, not an HTTP load test or production capacity claim.
The configured connection pool permits up to 24 connections. The test writes a
machine-readable result to `target/concurrency-results.json` only after assertions pass.

## Other verified behavior

- Four unit tests and three MySQL integration tests passed, with no failures/skips.
- Forced rollback after booking removed both appointment and outbox event and
  returned the slot to AVAILABLE.
- A real local HTTP stub returned 503: the committed appointment remained, the
  event stayed pending, and immediate re-delivery was suppressed by backoff.
- After restoring HTTP success and making the retry due, the event became SENT.
- Fresh test schema migrations succeeded, and repeat startup validated migrations.
- Existing clinic schema was backed up to `/tmp/medical_clinic_before_flyway.sql`,
  explicitly baselined at 0, and migrated to version 2. Existing appointment #14
  remained accessible through the authenticated confirmation page.
- An HTTP form smoke test created appointment #15, labeled
  "Asynchronous outbox verification 2026-09-22". The actual scheduled worker
  delivered its event and MySQL recorded SENT.

## Delivery limits

Delivery is at least once. A crash after remote acceptance but before recording SENT
can cause a duplicate attempt. A stable Idempotency-Key accompanies each attempt;
a real receiver needs durable deduplication. The mock provides stable response IDs,
not a production email/SMS delivery implementation. Worker HTTP calls use a separate
transaction that locks only the selected event; timeouts bound that transaction.
Retries continue with exponential backoff capped at one hour. There is no dead-letter
operations interface yet.

## Resume wording supported by this run

- Validated transactional booking integrity against MySQL with 200 attempts across
  10 contention rounds, enforcing one successful reservation per slot with zero
  duplicate appointments.
- Implemented a transactional outbox with asynchronous notification delivery,
  bounded HTTP timeouts, and exponential retry backoff; verified booking durability
  during simulated notification-service failures and successful recovery.
- Configured GitHub Actions to run unit and MySQL integration tests, apply versioned
  Flyway migrations, and publish concurrency results and test reports.

## Design references

- [Spring Boot 4 migration guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [MySQL locking reads and SKIP LOCKED](https://dev.mysql.com/doc/refman/8.4/en/select.html)
