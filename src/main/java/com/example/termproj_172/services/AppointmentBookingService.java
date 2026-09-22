package com.example.termproj_172.services;

import com.example.termproj_172.domainModels.Appointment;
import com.example.termproj_172.domainModels.AppointmentBookingForm;
import com.example.termproj_172.domainModels.AppointmentBookingResult;
import com.example.termproj_172.domainModels.AppUser;
import com.example.termproj_172.domainModels.NotificationResponse;
import com.example.termproj_172.domainModels.Patient;
import com.example.termproj_172.domainModels.TimeSlot;
import com.example.termproj_172.domainModels.TimeSlotStatus;
import com.example.termproj_172.repositories.AppointmentRepository;
import com.example.termproj_172.repositories.PatientRepository;
import com.example.termproj_172.repositories.TimeSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppointmentBookingService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentBookingService.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final NotificationOutbox notificationOutbox;
    private final CurrentUserService currentUserService;

    public AppointmentBookingService(AppointmentRepository appointmentRepository,
                                     PatientRepository patientRepository,
                                     TimeSlotRepository timeSlotRepository,
                                     NotificationOutbox notificationOutbox,
                                     CurrentUserService currentUserService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.notificationOutbox = notificationOutbox;
        this.currentUserService = currentUserService;
    }

    public List<TimeSlot> getAvailableTimeSlots() {
        return timeSlotRepository.findByStatusOrderByAppointmentDateAscStartTimeAsc(TimeSlotStatus.AVAILABLE);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AppointmentBookingResult bookAppointment(AppointmentBookingForm form, AppUser user) {
        long started = System.currentTimeMillis();
        normalizePatientForRole(form, user);
        logger.info("event=appointment_request_received patientId={} slotId={} message=\"Appointment booking requested\"",
                form.getPatientId(), form.getTimeSlotId());
        validate(form);

        Patient patient = patientRepository.findById(form.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found."));

        int claimedRows = timeSlotRepository.claimSlotIfAvailable(
                form.getTimeSlotId(),
                TimeSlotStatus.AVAILABLE,
                TimeSlotStatus.BOOKED
        );

        if (claimedRows == 0) {
            logger.error("event=appointment_booking_failed slotId={} reason=slot_already_reserved message=\"Appointment booking failed because slot was already taken\"",
                    form.getTimeSlotId());
            throw new IllegalArgumentException("That time slot was just booked. Choose another slot.");
        }

        logger.info("event=slot_reserved slotId={} patientId={} message=\"Time slot reserved successfully\"",
                form.getTimeSlotId(), patient.getId());

        TimeSlot claimedSlot = timeSlotRepository.findById(form.getTimeSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Time slot not found."));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setTimeSlot(claimedSlot);
        appointment.setDescription(form.getDescription().trim());
        appointment.setStatus("Scheduled");

        try {
            Appointment savedAppointment = appointmentRepository.save(appointment);
            logger.info("event=appointment_created appointmentId={} patientId={} providerId={} latencyMs={} message=\"Appointment successfully created\"",
                    savedAppointment.getId(),
                    savedAppointment.getPatient().getId(),
                    savedAppointment.getTimeSlot().getProvider().getId(),
                    System.currentTimeMillis() - started);
            notificationOutbox.enqueue(savedAppointment);
            return new AppointmentBookingResult(savedAppointment, new NotificationResponse("QUEUED", null));
        } catch (DataIntegrityViolationException exception) {
            logger.error("event=appointment_booking_failed slotId={} reason=constraint_collision message=\"Appointment booking failed during persistence\"",
                    form.getTimeSlotId(), exception);
            throw new IllegalArgumentException("That time slot was just booked. Choose another slot.", exception);
        }
    }

    private void normalizePatientForRole(AppointmentBookingForm form, AppUser user) {
        if (currentUserService.isPatient(user) && user.getPatient() != null) {
            form.setPatientId(user.getPatient().getId());
        }
    }

    private void validate(AppointmentBookingForm form) {
        if (form.getPatientId() == null) {
            logger.error("event=appointment_booking_failed reason=invalid_request message=\"Missing patient selection\"");
            throw new IllegalArgumentException("Please select a patient.");
        }
        if (form.getTimeSlotId() == null) {
            logger.error("event=appointment_booking_failed reason=invalid_request message=\"Missing time slot selection\"");
            throw new IllegalArgumentException("Please select an available time slot.");
        }
        if (form.getDescription() == null || form.getDescription().isBlank()) {
            logger.error("event=appointment_booking_failed reason=invalid_request message=\"Missing appointment reason\"");
            throw new IllegalArgumentException("Appointment reason is required.");
        }
    }
}
