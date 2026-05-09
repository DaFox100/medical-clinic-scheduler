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
        AppointmentConfirmationDTO dto = buildDto(appointment);
        ResponseEntity<NotificationResponse> response = restTemplate.postForEntity(
                notificationBaseUrl + "/mock-notification/send-confirmation",
                dto,
                NotificationResponse.class
        );

        NotificationResponse body = response.getBody();
        if (body == null) {
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
