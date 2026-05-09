package com.example.termproj_172.services;

import com.example.termproj_172.domainModels.AppUser;
import com.example.termproj_172.domainModels.Provider;
import com.example.termproj_172.domainModels.TimeSlot;
import com.example.termproj_172.repositories.DepartmentRepository;
import com.example.termproj_172.repositories.ProviderRepository;
import com.example.termproj_172.repositories.TimeSlotRepository;
import com.example.termproj_172.viewmodels.DepartmentScheduleView;
import com.example.termproj_172.viewmodels.HomeScheduleView;
import com.example.termproj_172.viewmodels.ProviderScheduleRowView;
import com.example.termproj_172.viewmodels.ProviderScheduleView;
import com.example.termproj_172.viewmodels.ScheduleCellView;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class TimeSlotService {

    private static final LocalTime CLINIC_OPEN = LocalTime.of(8, 0);
    private static final LocalTime CLINIC_CLOSE = LocalTime.of(17, 0);
    private static final int SLOT_INTERVAL_MINUTES = 30;
    private static final int DEFAULT_SCHEDULE_DAYS = 5;
    private static final int MAX_SCHEDULE_DAYS = 14;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE MMM d");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    private final TimeSlotRepository timeSlotRepository;
    private final ProviderRepository providerRepository;
    private final DepartmentRepository departmentRepository;
    private final CurrentUserService currentUserService;

    public TimeSlotService(TimeSlotRepository timeSlotRepository,
                           ProviderRepository providerRepository,
                           DepartmentRepository departmentRepository,
                           CurrentUserService currentUserService) {
        this.timeSlotRepository = timeSlotRepository;
        this.providerRepository = providerRepository;
        this.departmentRepository = departmentRepository;
        this.currentUserService = currentUserService;
    }

    public HomeScheduleView buildHomeSchedule(String departmentFilter,
                                              LocalDate requestedStartDate,
                                              LocalDate requestedEndDate,
                                              AppUser user) {
        LocalDate startDate = requestedStartDate != null ? requestedStartDate : LocalDate.now();
        LocalDate endDate = requestedEndDate != null ? requestedEndDate : startDate.plusDays(DEFAULT_SCHEDULE_DAYS - 1L);

        if (endDate.isBefore(startDate)) {
            endDate = startDate.plusDays(DEFAULT_SCHEDULE_DAYS - 1L);
        }

        if (startDate.plusDays(MAX_SCHEDULE_DAYS - 1L).isBefore(endDate)) {
            endDate = startDate.plusDays(MAX_SCHEDULE_DAYS - 1L);
        }

        List<Provider> providers = providerRepository.findAll().stream()
                .filter(provider -> departmentFilter == null
                        || departmentFilter.isBlank()
                        || provider.getDepartment().getName().equalsIgnoreCase(departmentFilter))
                .sorted(Comparator
                        .comparing((Provider provider) -> provider.getDepartment().getName())
                        .thenComparing(Provider::getFullName))
                .toList();

        List<TimeSlot> slotsInRange = timeSlotRepository.findAllByAppointmentDateBetween(startDate, endDate);

        Map<String, TimeSlot> slotLookup = slotsInRange.stream()
                .collect(Collectors.toMap(
                        slot -> key(slot.getProvider().getId(), slot.getAppointmentDate(), slot.getStartTime()),
                        slot -> slot
                ));

        List<LocalDate> dates = buildDates(startDate, endDate);
        List<LocalTime> times = buildTimes(slotsInRange);
        List<String> dateHeaders = dates.stream()
                .map(date -> date.format(DATE_FORMATTER))
                .toList();

        Map<String, List<ProviderScheduleView>> byDepartment = new LinkedHashMap<>();

        for (Provider provider : providers) {
            List<ProviderScheduleRowView> rows = new ArrayList<>();

            for (LocalTime time : times) {
                List<ScheduleCellView> cells = new ArrayList<>();

                for (LocalDate date : dates) {
                    TimeSlot slot = slotLookup.get(key(provider.getId(), date, time));
                    cells.add(toCellView(provider, date, time, slot, user));
                }

                rows.add(new ProviderScheduleRowView(time.format(TIME_FORMATTER), cells));
            }

            byDepartment.computeIfAbsent(provider.getDepartment().getName(), ignored -> new ArrayList<>())
                    .add(new ProviderScheduleView(provider.getFullName(), rows));
        }

        List<DepartmentScheduleView> departments = byDepartment.entrySet().stream()
                .map(entry -> new DepartmentScheduleView(entry.getKey(), entry.getValue()))
                .toList();

        return new HomeScheduleView(dateHeaders, departments);
    }

    public List<String> getDepartmentNames() {
        return departmentRepository.findAll().stream()
                .map(department -> department.getName())
                .sorted()
                .toList();
    }

    public TimeSlot getTimeSlot(Long slotId) {
        return timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Time slot not found."));
    }

    public TimeSlot getAvailableTimeSlot(Long slotId) {
        TimeSlot slot = getTimeSlot(slotId);
        if (!slot.isAvailable()) {
            throw new IllegalArgumentException("That time slot is no longer available.");
        }
        return slot;
    }

    private ScheduleCellView toCellView(Provider provider, LocalDate date, LocalTime time, TimeSlot slot, AppUser user) {
        String dateLabel = date.format(DATE_FORMATTER);
        String timeLabel = time.format(TIME_FORMATTER);
        LocalTime endTime = time.plusMinutes(SLOT_INTERVAL_MINUTES);

        if (slot == null) {
            return new ScheduleCellView(
                    null,
                    provider.getId(),
                    "EMPTY",
                    null,
                    provider.getDepartment().getName(),
                    dateLabel,
                    timeLabel,
                    date,
                    time,
                    endTime,
                    false,
                    canCreateDepartmentSlot(user, provider)
            );
        }

        return new ScheduleCellView(
                slot.getId(),
                provider.getId(),
                slot.getStatus().name(),
                provider.getFullName(),
                provider.getDepartment().getName(),
                dateLabel,
                timeLabel,
                date,
                slot.getStartTime(),
                slot.getEndTime(),
                true,
                false
        );
    }

    private boolean canCreateDepartmentSlot(AppUser user, Provider provider) {
        return currentUserService.isProvider(user)
                && user.getProvider() != null
                && user.getProvider().getDepartment().getId().equals(provider.getDepartment().getId());
    }

    private List<LocalDate> buildDates(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dates.add(date);
        }
        return dates;
    }

    private List<LocalTime> buildTimes(List<TimeSlot> slotsInRange) {
        TreeSet<LocalTime> times = new TreeSet<>();

        for (LocalTime time = CLINIC_OPEN; time.isBefore(CLINIC_CLOSE); time = time.plusMinutes(SLOT_INTERVAL_MINUTES)) {
            times.add(time);
        }

        for (TimeSlot slot : slotsInRange) {
            times.add(slot.getStartTime());
        }

        return new ArrayList<>(times);
    }

    private String key(Long providerId, LocalDate date, LocalTime time) {
        return providerId + "|" + date + "|" + time;
    }
}
