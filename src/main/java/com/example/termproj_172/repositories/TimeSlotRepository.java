package com.example.termproj_172.repositories;

import com.example.termproj_172.domainModels.TimeSlot;
import com.example.termproj_172.domainModels.TimeSlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findAllByAppointmentDateBetween(LocalDate startDate, LocalDate endDate);

    List<TimeSlot> findByStatusOrderByAppointmentDateAscStartTimeAsc(TimeSlotStatus status);
    List<TimeSlot> findByProviderIdOrderByAppointmentDateAscStartTimeAsc(Long providerId);
    List<TimeSlot> findByProvider_Department_IdOrderByAppointmentDateAscStartTimeAsc(Long departmentId);

    List<TimeSlot> findAllByOrderByAppointmentDateAscStartTimeAsc();

    @Query("""
        select count(ts) > 0
          from TimeSlot ts
         where ts.provider.id = :providerId
           and ts.appointmentDate = :appointmentDate
           and ts.startTime < :endTime
           and ts.endTime > :startTime
        """)
    boolean existsOverlappingSlot(@Param("providerId") Long providerId,
                                  @Param("appointmentDate") LocalDate appointmentDate,
                                  @Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime);

    @Modifying
    @Query("""
        update TimeSlot ts
           set ts.status = :bookedStatus
         where ts.id = :slotId
           and ts.status = :availableStatus
        """)
    int claimSlotIfAvailable(@Param("slotId") Long slotId,
                             @Param("availableStatus") TimeSlotStatus availableStatus,
                             @Param("bookedStatus") TimeSlotStatus bookedStatus);
}
