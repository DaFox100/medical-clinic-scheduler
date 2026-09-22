package com.example.termproj_172.integration;

import com.example.termproj_172.domainModels.*;
import com.example.termproj_172.repositories.*;
import com.example.termproj_172.services.*;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"app.seed.enabled=false", "app.notification.worker-enabled=false",
        "spring.jpa.show-sql=false", "spring.flyway.baseline-on-migrate=false",
        "spring.datasource.hikari.maximum-pool-size=24"})
class BookingMySqlIT {
    static final AtomicBoolean outage = new AtomicBoolean();
    static final AtomicInteger deliveries = new AtomicInteger();
    static final HttpServer server;
    static {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/mock-notification/send-confirmation", exchange -> {
                exchange.getRequestBody().readAllBytes();
                deliveries.incrementAndGet();
                byte[] body = "{\"status\":\"SENT\",\"messageId\":\"test-message\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(outage.get() ? 503 : 200, body.length);
                try (var stream = exchange.getResponseBody()) { stream.write(body); }
            });
            server.start();
        } catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        String url = System.getenv().getOrDefault("TEST_DB_URL", "jdbc:mysql://localhost:3306/medical_clinic_test?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false");
        if (!url.matches("jdbc:mysql://[^/]+/medical_clinic_test(?:\\?.*)?"))
            throw new IllegalArgumentException("Integration tests require the dedicated medical_clinic_test database");
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("TEST_DB_USERNAME", "root"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("TEST_DB_PASSWORD", ""));
        registry.add("app.notification.base-url", () -> "http://127.0.0.1:" + server.getAddress().getPort());
    }
    @Autowired AppointmentBookingService booking;
    @Autowired NotificationDispatcher dispatcher;
    @Autowired JdbcTemplate jdbc;
    @Autowired DepartmentRepository departments;
    @Autowired ProviderRepository providers;
    @Autowired PatientRepository patients;
    @Autowired TimeSlotRepository slots;
    @Autowired PlatformTransactionManager transactions;
    Patient patient;
    Provider provider;
    AppUser admin;

    @BeforeEach void setup() {
        jdbc.update("DELETE FROM notification_outbox");
        jdbc.update("DELETE FROM appointments");
        jdbc.update("DELETE FROM app_users");
        jdbc.update("DELETE FROM time_slots");
        jdbc.update("DELETE FROM providers");
        jdbc.update("DELETE FROM patients");
        jdbc.update("DELETE FROM departments");
        outage.set(false); deliveries.set(0);
        var department = departments.save(new Department("Integration"));
        provider = providers.save(new Provider("Test Provider", department));
        patient = patients.save(new Patient("Test Patient", "integration@example.com"));
        admin = new AppUser(); admin.setRole(AppUserRole.ADMIN);
    }
    @AfterAll static void stopServer() { server.stop(0); }
    TimeSlot slot(int index) {
        return slots.save(new TimeSlot(provider, LocalDate.now().plusDays(index + 1),
                LocalTime.of(9, 0), LocalTime.of(9, 30), TimeSlotStatus.AVAILABLE));
    }
    AppointmentBookingForm form(TimeSlot slot) {
        var form = new AppointmentBookingForm();
        form.setPatientId(patient.getId()); form.setTimeSlotId(slot.getId());
        form.setDescription("Integration verification"); return form;
    }
    long count(String table) { return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Long.class); }

    @Test void twentyConcurrentAttemptsAcrossTenRoundsCreateExactlyOneBookingPerSlot() throws Exception {
        int rounds = 10, concurrency = 20, successes = 0, conflicts = 0;
        var executor = Executors.newFixedThreadPool(concurrency);
        long started = System.nanoTime();
        try {
            for (int round = 0; round < rounds; round++) {
                var slot = slot(round);
                var barrier = new CyclicBarrier(concurrency);
                List<Future<Boolean>> results = new ArrayList<>();
                for (int i = 0; i < concurrency; i++) results.add(executor.submit(() -> {
                    barrier.await(15, TimeUnit.SECONDS);
                    try { booking.bookAppointment(form(slot), admin); return true; }
                    catch (IllegalArgumentException e) {
                        assertEquals("That time slot was just booked. Choose another slot.", e.getMessage());
                        return false;
                    }
                }));
                int winners = 0;
                for (var result : results) { if (result.get(30, TimeUnit.SECONDS)) { winners++; successes++; } else conflicts++; }
                assertEquals(1, winners);
                assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM appointments WHERE time_slot_id=?", Integer.class, slot.getId()));
                assertEquals(TimeSlotStatus.BOOKED, slots.findById(slot.getId()).orElseThrow().getStatus());
            }
        } finally { executor.shutdownNow(); }
        assertEquals(rounds, count("appointments"));
        assertEquals(rounds, count("notification_outbox"));
        assertEquals(0, deliveries.get(), "Booking must not perform notification HTTP calls");
        Files.createDirectories(Path.of("target"));
        Files.writeString(Path.of("target/concurrency-results.json"), String.format(Locale.ROOT,
                "{\"rounds\":%d,\"concurrentAttemptsPerRound\":%d,\"totalAttempts\":%d,\"successfulBookings\":%d,\"rejectedConflicts\":%d,\"duplicateBookings\":0,\"elapsedSeconds\":%.3f,\"scope\":\"local service-layer calls against MySQL; not HTTP load testing\"}%n",
                rounds, concurrency, rounds * concurrency, successes, conflicts, (System.nanoTime()-started)/1e9));
    }

    @Test void rollbackRemovesAppointmentAndEventAndReleasesSlot() {
        var slot = slot(0);
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactions).execute(status -> {
            booking.bookAppointment(form(slot), admin);
            throw new IllegalStateException("Simulated failure before commit");
        }));
        assertEquals(0, count("appointments")); assertEquals(0, count("notification_outbox"));
        assertEquals(TimeSlotStatus.AVAILABLE, slots.findById(slot.getId()).orElseThrow().getStatus());
    }

    @Test void notificationOutageDoesNotUndoBookingAndRetryRecovers() {
        outage.set(true);
        booking.bookAppointment(form(slot(0)), admin);
        assertEquals(0, deliveries.get()); assertEquals(1, count("appointments"));
        assertTrue(dispatcher.dispatchOne());
        assertEquals("PENDING", jdbc.queryForObject("SELECT status FROM notification_outbox", String.class));
        assertEquals(1, jdbc.queryForObject("SELECT attempts FROM notification_outbox", Integer.class));
        assertFalse(dispatcher.dispatchOne(), "Retry must wait until due");
        outage.set(false);
        jdbc.update("UPDATE notification_outbox SET next_attempt_at = CURRENT_TIMESTAMP(6)");
        assertTrue(dispatcher.dispatchOne());
        assertEquals("SENT", jdbc.queryForObject("SELECT status FROM notification_outbox", String.class));
        assertEquals(2, jdbc.queryForObject("SELECT attempts FROM notification_outbox", Integer.class));
        assertEquals(1, count("appointments")); assertFalse(dispatcher.dispatchOne());
        assertEquals(2, deliveries.get());
    }
}
