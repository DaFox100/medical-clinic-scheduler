package com.example.termproj_172.domainModels;

import java.time.LocalDate;
import java.time.LocalTime;

public class TimeSlotManagementForm {

    private Long providerId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private TimeSlotStatus status = TimeSlotStatus.AVAILABLE;

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public TimeSlotStatus getStatus() {
        return status;
    }

    public void setStatus(TimeSlotStatus status) {
        this.status = status;
    }
}
