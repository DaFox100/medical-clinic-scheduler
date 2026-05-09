package com.example.termproj_172.services;

import com.example.termproj_172.domainModels.Appointment;
import com.example.termproj_172.domainModels.AppUser;
import com.example.termproj_172.domainModels.TimeSlot;
import com.example.termproj_172.domainModels.TimeSlotStatus;
import com.example.termproj_172.repositories.AppointmentRepository;
import com.example.termproj_172.repositories.TimeSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppointmentRescheduleService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentRescheduleService.class);

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final CurrentUserService currentUserService;

    public AppointmentRescheduleService(AppointmentRepository appointmentRepository,
                                        TimeSlotRepository timeSlotRepository,
                                        CurrentUserService currentUserService) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.currentUserService = currentUserService;
    }

    public List<TimeSlot> getAvailableTimeSlotsForReschedule(Long currentSlotId) {
        return timeSlotRepository.findByStatusOrderByAppointmentDateAscStartTimeAsc(TimeSlotStatus.AVAILABLE).stream()
                .filter(slot -> !slot.getId().equals(currentSlotId))
                .toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Appointment rescheduleAppointment(Long appointmentId, Long newSlotId, AppUser user) {
        logger.info("event=appointment_reschedule_requested appointmentId={} newSlotId={} message=\"Appointment reschedule requested\"",
                appointmentId, newSlotId);
        if (newSlotId == null) {
            logger.error("event=appointment_reschedule_failed appointmentId={} reason=invalid_request message=\"Missing new time slot selection\"",
                    appointmentId);
            throw new IllegalArgumentException("Please select a new time slot.");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));
        validateAccess(appointment, user);

        Long currentSlotId = appointment.getTimeSlot().getId();
        if (currentSlotId.equals(newSlotId)) {
            logger.error("event=appointment_reschedule_failed appointmentId={} reason=same_slot message=\"Reschedule requested for same slot\"",
                    appointmentId);
            throw new IllegalArgumentException("Choose a different time slot to reschedule.");
        }

        int claimedRows = timeSlotRepository.claimSlotIfAvailable(
                newSlotId,
                TimeSlotStatus.AVAILABLE,
                TimeSlotStatus.BOOKED
        );

        if (claimedRows == 0) {
            logger.error("event=appointment_reschedule_failed appointmentId={} slotId={} reason=slot_already_reserved message=\"New time slot is no longer available\"",
                    appointmentId, newSlotId);
            throw new IllegalArgumentException("That new time slot is no longer available.");
        }

        TimeSlot oldSlot = appointment.getTimeSlot();
        TimeSlot newSlot = timeSlotRepository.findById(newSlotId)
                .orElseThrow(() -> new IllegalArgumentException("Time slot not found."));

        appointment.setTimeSlot(newSlot);
        appointmentRepository.save(appointment);

        oldSlot.setStatus(TimeSlotStatus.AVAILABLE);
        timeSlotRepository.save(oldSlot);

        logger.info("event=appointment_rescheduled appointmentId={} oldSlotId={} newSlotId={} patientId={} message=\"Appointment successfully rescheduled\"",
                appointmentId, currentSlotId, newSlotId, appointment.getPatient().getId());

        return appointment;
    }

    private void validateAccess(Appointment appointment, AppUser user) {
        if (currentUserService.isAdmin(user)) {
            return;
        }
        if (currentUserService.isProvider(user) && user.getProvider() != null
                && appointment.getTimeSlot().getProvider().getId().equals(user.getProvider().getId())) {
            return;
        }
        if (currentUserService.isPatient(user) && user.getPatient() != null
                && appointment.getPatient().getId().equals(user.getPatient().getId())) {
            return;
        }
        throw new IllegalArgumentException("You do not have access to reschedule this appointment.");
    }
}
