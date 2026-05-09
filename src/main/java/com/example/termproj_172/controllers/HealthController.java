package com.example.termproj_172.controllers;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    private static final long DEGRADED_DB_LATENCY_MS = 1500L;

    private final ObjectProvider<DataSource> dataSourceProvider;
    private final Instant startedAt = Instant.now();

    public HealthController(ObjectProvider<DataSource> dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        logger.info("event=health_check_requested message=\"Health check requested\"");
        DatabaseHealth databaseHealth = checkMySqlConnection();

        Map<String, String> services = new HashMap<>();
        services.put("application", "UP");
        services.put("database", databaseHealth.up() ? "UP" : "DOWN");

        Map<String, Object> response = new HashMap<>();
        String status = determineStatus(databaseHealth);
        response.put("status", status);
        response.put("timestamp", Instant.now().toString());
        response.put("services", services);
        response.put("metrics", Map.of(
                "databaseLatencyMs", databaseHealth.latencyMs(),
                "uptimeSeconds", Duration.between(startedAt, Instant.now()).toSeconds()
        ));

        if (!databaseHealth.up()) {
            logger.warn("event=database_connection_failure service=database latencyMs={} message=\"Database health check failed\"",
                    databaseHealth.latencyMs());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }

        if ("DEGRADED".equals(status)) {
            logger.warn("event=database_connection_slow service=database latencyMs={} message=\"Database health check exceeded latency threshold\"",
                    databaseHealth.latencyMs());
        } else {
            logger.info("event=health_check_success service=database latencyMs={} message=\"Health check passed\"",
                    databaseHealth.latencyMs());
        }

        return ResponseEntity.ok(response);
    }

    private String determineStatus(DatabaseHealth databaseHealth) {
        if (!databaseHealth.up()) {
            return "DOWN";
        }
        if (databaseHealth.latencyMs() > DEGRADED_DB_LATENCY_MS) {
            return "DEGRADED";
        }
        return "UP";
    }

    private DatabaseHealth checkMySqlConnection() {
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            return new DatabaseHealth(false, -1L);
        }

        long started = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            return new DatabaseHealth(valid, System.currentTimeMillis() - started);
        } catch (Exception exception) {
            return new DatabaseHealth(false, System.currentTimeMillis() - started);
        }
    }

    private record DatabaseHealth(boolean up, long latencyMs) {
    }
}
