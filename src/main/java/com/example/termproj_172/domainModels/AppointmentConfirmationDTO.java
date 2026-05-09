package com.example.termproj_172.domainModels;

public class AppointmentConfirmationDTO {
    private Long appointmentId;
    private String patientName;
    private String patientEmail;
    private String doctorName;
    private String appointmentTime;

    public AppointmentConfirmationDTO() {}

    public AppointmentConfirmationDTO(Long appointmentId, String patientName,
                                      String patientEmail, String doctorName,
                                      String appointmentTime) {
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.doctorName = doctorName;
        this.appointmentTime = appointmentTime;
    }

    public Long getAppointmentId() { return appointmentId; }
    public String getPatientName() { return patientName; }
    public String getPatientEmail() { return patientEmail; }
    public String getDoctorName() { return doctorName; }
    public String getAppointmentTime() { return appointmentTime; }

    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
}
