package com.example.termproj_172.services;

import com.example.termproj_172.domainModels.Appointment;
import com.example.termproj_172.domainModels.AppointmentConfirmationDTO;
import com.example.termproj_172.domainModels.NotificationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ConfirmationPageService {

    private final RestTemplate restTemplate;
    private final String notificationBaseUrl;

    public ConfirmationPageService(RestTemplate restTemplate,
                                   @Value("${app.notification.base-url:http://localhost:8080}") String notificationBaseUrl) {
        this.restTemplate = restTemplate;
        this.notificationBaseUrl = notificationBaseUrl;
    }

    public NotificationResponse sendConfirmation(Appointment appointment) {
        return sendConfirmation(buildDto(appointment), "appointment-" + appointment.getId());
    }

    public NotificationResponse sendConfirmation(AppointmentConfirmationDTO dto, String eventId) {
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("Idempotency-Key", eventId);
        ResponseEntity<NotificationResponse> response = restTemplate.postForEntity(
                notificationBaseUrl + "/mock-notification/send-confirmation",
                new org.springframework.http.HttpEntity<>(dto, headers),
                NotificationResponse.class
        );

        NotificationResponse body = response.getBody();
        if (body == null || !"SENT".equals(body.getStatus())) {
            throw new IllegalStateException("Mock notification service returned an empty response.");
        }

        return body;
    }

    public AppointmentConfirmationDTO buildDto(Appointment appointment) {
        return new AppointmentConfirmationDTO(
                appointment.getId(),
                appointment.getPatient().getFullName(),
                appointment.getPatient().getEmail(),
                appointment.getTimeSlot().getProvider().getFullName(),
                appointment.getTimeSlot().getAppointmentDate() + " " + appointment.getTimeSlot().getStartTime()
        );
    }
}
