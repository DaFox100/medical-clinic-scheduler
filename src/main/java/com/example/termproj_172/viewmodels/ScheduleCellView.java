package com.example.termproj_172.viewmodels;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ScheduleCellView {

    private static final DateTimeFormatter DATE_VALUE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_VALUE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Long slotId;
    private final Long providerId;
    private final String status;
    private final String providerName;
    private final String departmentName;
    private final String dateLabel;
    private final String timeLabel;
    private final LocalDate appointmentDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final boolean slotClickable;
    private final boolean emptyCreationAllowed;

    public ScheduleCellView(Long slotId,
                            Long providerId,
                            String status,
                            String providerName,
                            String departmentName,
                            String dateLabel,
                            String timeLabel,
                            LocalDate appointmentDate,
                            LocalTime startTime,
                            LocalTime endTime,
                            boolean slotClickable,
                            boolean emptyCreationAllowed) {
        this.slotId = slotId;
        this.providerId = providerId;
        this.status = status;
        this.providerName = providerName;
        this.departmentName = departmentName;
        this.dateLabel = dateLabel;
        this.timeLabel = timeLabel;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotClickable = slotClickable;
        this.emptyCreationAllowed = emptyCreationAllowed;
    }

    public Long getSlotId() {
        return slotId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public String getStatus() {
        return status;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getDateLabel() {
        return dateLabel;
    }

    public String getTimeLabel() {
        return timeLabel;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public String getAppointmentDateValue() {
        return appointmentDate == null ? null : appointmentDate.format(DATE_VALUE_FORMATTER);
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public String getStartTimeValue() {
        return startTime == null ? null : startTime.format(TIME_VALUE_FORMATTER);
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getEndTimeValue() {
        return endTime == null ? null : endTime.format(TIME_VALUE_FORMATTER);
    }

    public boolean isSlotClickable() {
        return slotClickable;
    }

    public boolean isEmptyCreationAllowed() {
        return emptyCreationAllowed;
    }

    public boolean isEmpty() {
        return "EMPTY".equals(status);
    }
}
