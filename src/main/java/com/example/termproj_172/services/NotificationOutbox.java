package com.example.termproj_172.services;

import com.example.termproj_172.domainModels.Appointment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class NotificationOutbox {
    private final JdbcTemplate jdbc;
    private final ConfirmationPageService confirmations;

    public NotificationOutbox(JdbcTemplate jdbc, ConfirmationPageService confirmations) {
        this.jdbc = jdbc;
        this.confirmations = confirmations;
    }

    // Shares the JPA transaction and connection: appointment and event commit together.
    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(Appointment appointment) {
        var dto = confirmations.buildDto(appointment);
        jdbc.update("""
                INSERT INTO notification_outbox
                (id, appointment_id, patient_name, patient_email, doctor_name, appointment_time)
                VALUES (?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID().toString(), dto.getAppointmentId(), dto.getPatientName(),
                dto.getPatientEmail(), dto.getDoctorName(), dto.getAppointmentTime());
    }
}
