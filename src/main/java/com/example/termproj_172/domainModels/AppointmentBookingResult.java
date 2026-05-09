package com.example.termproj_172.domainModels;

public class AppointmentBookingResult {

    private final Appointment appointment;
    private final NotificationResponse notificationResponse;

    public AppointmentBookingResult(Appointment appointment, NotificationResponse notificationResponse) {
        this.appointment = appointment;
        this.notificationResponse = notificationResponse;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public NotificationResponse getNotificationResponse() {
        return notificationResponse;
    }
}
