CREATE TABLE notification_outbox (
 id VARCHAR(36) PRIMARY KEY,
 appointment_id BIGINT NOT NULL UNIQUE,
 patient_name VARCHAR(100) NOT NULL,
 patient_email VARCHAR(120) NOT NULL,
 doctor_name VARCHAR(100) NOT NULL,
 appointment_time VARCHAR(80) NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
 attempts INT NOT NULL DEFAULT 0,
 next_attempt_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 sent_at TIMESTAMP(6) NULL,
 last_error VARCHAR(200),
 FOREIGN KEY (appointment_id) REFERENCES appointments(id),
 INDEX ix_outbox_due (status, next_attempt_at)
) ENGINE=InnoDB;
