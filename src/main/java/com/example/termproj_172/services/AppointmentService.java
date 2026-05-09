package com.example.termproj_172.services;

import com.example.termproj_172.domainModels.AppUser;
import com.example.termproj_172.domainModels.Appointment;
import com.example.termproj_172.domainModels.Patient;
import com.example.termproj_172.repositories.AppointmentRepository;
import com.example.termproj_172.repositories.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final CurrentUserService currentUserService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              CurrentUserService currentUserService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.currentUserService = currentUserService;
    }

    public List<Appointment> getVisibleAppointments(AppUser user) {
        if (currentUserService.isAdmin(user)) {
            return appointmentRepository.findAllByOrderByTimeSlotAppointmentDateAscTimeSlotStartTimeAsc();
        }
        if (currentUserService.isProvider(user) && user.getProvider() != null) {
            return appointmentRepository.findByTimeSlotProviderIdOrderByTimeSlotAppointmentDateAscTimeSlotStartTimeAsc(
                    user.getProvider().getId()
            );
        }
        if (currentUserService.isPatient(user) && user.getPatient() != null) {
            return appointmentRepository.findByPatientIdOrderByTimeSlotAppointmentDateAscTimeSlotStartTimeAsc(
                    user.getPatient().getId()
            );
        }
        return List.of();
    }

    public Appointment getAccessibleAppointment(Long id, AppUser user) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));

        if (currentUserService.isAdmin(user)) {
            return appointment;
        }
        if (currentUserService.isProvider(user) && user.getProvider() != null
                && appointment.getTimeSlot().getProvider().getId().equals(user.getProvider().getId())) {
            return appointment;
        }
        if (currentUserService.isPatient(user) && user.getPatient() != null
                && appointment.getPatient().getId().equals(user.getPatient().getId())) {
            return appointment;
        }

        throw new IllegalArgumentException("You do not have access to this appointment.");
    }

    public List<Patient> getVisiblePatientsForBooking(AppUser user) {
        if (currentUserService.isPatient(user) && user.getPatient() != null) {
            return List.of(user.getPatient());
        }
        return patientRepository.findAll();
    }

    public String getAppointmentViewTitle(AppUser user) {
        if (currentUserService.isPatient(user)) {
            return "My Appointments";
        }
        if (currentUserService.isProvider(user)) {
            return "Provider Appointments";
        }
        return "All Appointments";
    }

    public String getAppointmentViewCopy(AppUser user) {
        if (currentUserService.isPatient(user)) {
            return "Review your booked visits and reschedule them when needed.";
        }
        if (currentUserService.isProvider(user)) {
            return "Review appointments assigned to your time slots.";
        }
        return "Review the full clinic schedule across all patients and providers.";
    }
}
