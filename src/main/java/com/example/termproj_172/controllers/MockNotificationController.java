package com.example.termproj_172.controllers;

import com.example.termproj_172.domainModels.AppointmentConfirmationDTO;
import com.example.termproj_172.domainModels.NotificationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.UUID;

@RestController
public class MockNotificationController {

    @PostMapping("/mock-notification/send-confirmation")
    public ResponseEntity<NotificationResponse> sendConfirmation(@RequestBody AppointmentConfirmationDTO dto,
            @org.springframework.web.bind.annotation.RequestHeader(value = "Idempotency-Key", required = false) String eventId) {
        String patientNamePart = dto.getPatientName() == null
                ? "PATIENT"
                : dto.getPatientName().replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        String messageId = "MSG-" + patientNamePart + "-" + (eventId == null ? UUID.randomUUID() : UUID.nameUUIDFromBytes(eventId.getBytes(java.nio.charset.StandardCharsets.UTF_8))).toString().toUpperCase(Locale.ROOT);

        return ResponseEntity.ok(new NotificationResponse("SENT", messageId));
    }
}
