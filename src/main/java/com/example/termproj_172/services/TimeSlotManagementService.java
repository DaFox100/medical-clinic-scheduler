package com.example.termproj_172.services;

import com.example.termproj_172.domainModels.AppUser;
import com.example.termproj_172.domainModels.Provider;
import com.example.termproj_172.domainModels.TimeSlot;
import com.example.termproj_172.domainModels.TimeSlotManagementForm;
import com.example.termproj_172.domainModels.TimeSlotStatus;
import com.example.termproj_172.repositories.AppointmentRepository;
import com.example.termproj_172.repositories.ProviderRepository;
import com.example.termproj_172.repositories.TimeSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TimeSlotManagementService {

    private static final Logger logger = LoggerFactory.getLogger(TimeSlotManagementService.class);

    private final TimeSlotRepository timeSlotRepository;
    private final ProviderRepository providerRepository;
    private final AppointmentRepository appointmentRepository;
    private final CurrentUserService currentUserService;

    public TimeSlotManagementService(TimeSlotRepository timeSlotRepository,
                                     ProviderRepository providerRepository,
                                     AppointmentRepository appointmentRepository,
                                     CurrentUserService currentUserService) {
        this.timeSlotRepository = timeSlotRepository;
        this.providerRepository = providerRepository;
        this.appointmentRepository = appointmentRepository;
        this.currentUserService = currentUserService;
    }

    public List<TimeSlot> getVisibleTimeSlots(AppUser user) {
        if (currentUserService.isProvider(user) && user.getProvider() != null) {
            return timeSlotRepository.findByProvider_Department_IdOrderByAppointmentDateAscStartTimeAsc(
                    user.getProvider().getDepartment().getId()
            );
        }
        return timeSlotRepository.findAllByOrderByAppointmentDateAscStartTimeAsc();
    }

    public List<Provider> getVisibleProviders(AppUser user) {
        if (currentUserService.isProvider(user) && user.getProvider() != null) {
            return providerRepository.findAllByDepartment_IdOrderByFullNameAsc(user.getProvider().getDepartment().getId());
        }
        return providerRepository.findAllByOrderByDepartment_NameAscFullNameAsc();
    }

    public TimeSlotManagementForm buildPrefilledForm(AppUser user,
                                                     Long providerId,
                                                     java.time.LocalDate appointmentDate,
                                                     java.time.LocalTime startTime,
                                                     java.time.LocalTime endTime) {
        TimeSlotManagementForm form = new TimeSlotManagementForm();

        if (providerId != null) {
            Provider provider = providerRepository.findById(providerId)
                    .orElseThrow(() -> new IllegalArgumentException("Provider not found."));
            ensureCanManageProvider(provider, user);
            form.setProviderId(provider.getId());
        } else if (currentUserService.isProvider(user) && user.getProvider() != null) {
            form.setProviderId(user.getProvider().getId());
        }

        form.setAppointmentDate(appointmentDate);
        form.setStartTime(startTime);
        form.setEndTime(endTime);
        form.setStatus(TimeSlotStatus.AVAILABLE);
        return form;
    }

    @Transactional
    public TimeSlot createTimeSlot(TimeSlotManagementForm form, AppUser user) {
        validate(form);

        Provider provider = providerRepository.findById(form.getProviderId())
                .orElseThrow(() -> new IllegalArgumentException("Provider not found."));
        ensureCanManageProvider(provider, user);

        boolean overlaps = timeSlotRepository.existsOverlappingSlot(
                provider.getId(),
                form.getAppointmentDate(),
                form.getStartTime(),
                form.getEndTime()
        );

        if (overlaps) {
            logger.error("event=time_slot_create_failed providerId={} date={} startTime={} endTime={} reason=overlap message=\"Overlapping time slot rejected\"",
                    form.getProviderId(), form.getAppointmentDate(), form.getStartTime(), form.getEndTime());
            throw new IllegalArgumentException("This provider already has an overlapping slot in that time range.");
        }

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setProvider(provider);
        timeSlot.setAppointmentDate(form.getAppointmentDate());
        timeSlot.setStartTime(form.getStartTime());
        timeSlot.setEndTime(form.getEndTime());
        timeSlot.setStatus(form.getStatus() == null ? TimeSlotStatus.AVAILABLE : form.getStatus());

        TimeSlot saved = timeSlotRepository.save(timeSlot);
        logger.info("event=time_slot_created slotId={} providerId={} date={} startTime={} endTime={} status={} message=\"Time slot created successfully\"",
                saved.getId(), saved.getProvider().getId(), saved.getAppointmentDate(), saved.getStartTime(), saved.getEndTime(), saved.getStatus());
        return saved;
    }

    @Transactional
    public void updateStatus(Long slotId, TimeSlotStatus status, AppUser user) {
        if (status == null) {
            logger.error("event=time_slot_status_update_failed slotId={} reason=invalid_request message=\"Missing status value\"",
                    slotId);
            throw new IllegalArgumentException("Status is required.");
        }

        if (appointmentRepository.existsByTimeSlotId(slotId)) {
            logger.error("event=time_slot_status_update_failed slotId={} reason=slot_booked message=\"Booked time slot status change rejected\"",
                    slotId);
            throw new IllegalArgumentException("Booked time slots cannot have their status changed manually.");
        }

        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Time slot not found."));
        ensureCanManageSlot(timeSlot, user);

        timeSlot.setStatus(status);
        timeSlotRepository.save(timeSlot);
        logger.info("event=time_slot_status_updated slotId={} status={} message=\"Time slot status updated\"",
                slotId, status);
    }

    @Transactional
    public void deleteUnusedSlot(Long slotId, AppUser user) {
        if (appointmentRepository.existsByTimeSlotId(slotId)) {
            logger.error("event=time_slot_delete_failed slotId={} reason=slot_booked message=\"Booked time slot deletion rejected\"",
                    slotId);
            throw new IllegalArgumentException("Booked time slots cannot be deleted.");
        }

        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Time slot not found."));
        ensureCanManageSlot(timeSlot, user);

        timeSlotRepository.deleteById(slotId);
        logger.info("event=time_slot_deleted slotId={} message=\"Unused time slot deleted\"",
                slotId);
    }

    private void ensureCanManageSlot(TimeSlot timeSlot, AppUser user) {
        ensureCanManageProvider(timeSlot.getProvider(), user);
    }

    private void ensureCanManageProvider(Provider provider, AppUser user) {
        if (currentUserService.isAdmin(user)) {
            return;
        }
        if (currentUserService.isProvider(user) && user.getProvider() != null
                && user.getProvider().getDepartment().getId().equals(provider.getDepartment().getId())) {
            return;
        }
        throw new IllegalArgumentException("You do not have access to manage this provider's time slots.");
    }

    private void validate(TimeSlotManagementForm form) {
        if (form.getProviderId() == null) {
            throw new IllegalArgumentException("Please select a provider.");
        }
        if (form.getAppointmentDate() == null) {
            throw new IllegalArgumentException("Appointment date is required.");
        }
        if (form.getStartTime() == null || form.getEndTime() == null) {
            throw new IllegalArgumentException("Start and end times are required.");
        }
        if (!form.getEndTime().isAfter(form.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
    }
}
