package com.example.termproj_172.services;

import com.example.termproj_172.domainModels.AppointmentConfirmationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDispatcher {
    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);
    private final JdbcTemplate jdbc;
    private final ConfirmationPageService confirmations;
    private final int retrySeconds;

    public NotificationDispatcher(JdbcTemplate jdbc, ConfirmationPageService confirmations,
            @Value("${app.notification.retry-seconds:5}") int retrySeconds) {
        this.jdbc = jdbc;
        this.confirmations = confirmations;
        this.retrySeconds = Math.max(1, retrySeconds);
    }

    // A separate, bounded worker transaction; never holds booking/slot locks.
    // SKIP LOCKED allows other workers to process different events.
    @Transactional
    public boolean dispatchOne() {
        var events = jdbc.query("""
                SELECT * FROM notification_outbox
                WHERE status = 'PENDING' AND next_attempt_at <= CURRENT_TIMESTAMP(6)
                ORDER BY next_attempt_at, id LIMIT 1 FOR UPDATE SKIP LOCKED
                """, (rs, row) -> new Event(rs.getString("id"), rs.getInt("attempts"),
                new AppointmentConfirmationDTO(rs.getLong("appointment_id"),
                        rs.getString("patient_name"), rs.getString("patient_email"),
                        rs.getString("doctor_name"), rs.getString("appointment_time"))));
        if (events.isEmpty()) return false;
        var event = events.get(0);
        try {
            confirmations.sendConfirmation(event.payload(), event.id());
        } catch (RuntimeException failure) {
            long delay = Math.min(3600L, retrySeconds * (1L << Math.min(event.attempts(), 10)));
            jdbc.update("""
                    UPDATE notification_outbox SET attempts = attempts + 1,
                    next_attempt_at = TIMESTAMPADD(SECOND, ?, CURRENT_TIMESTAMP(6)), last_error = ?
                    WHERE id = ?
                    """, delay, failure.getClass().getSimpleName(), event.id());
            log.warn("event=notification_retry_scheduled eventId={} delaySeconds={} error={}",
                    event.id(), delay, failure.getClass().getSimpleName());
            return true;
        }
        jdbc.update("""
                UPDATE notification_outbox SET status = 'SENT', attempts = attempts + 1,
                sent_at = CURRENT_TIMESTAMP(6), last_error = NULL WHERE id = ?
                """, event.id());
        log.info("event=notification_delivered eventId={}", event.id());
        return true;
    }

    private record Event(String id, int attempts, AppointmentConfirmationDTO payload) {}
}
