package com.example.termproj_172.repositories;

import com.example.termproj_172.domainModels.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findAllByOrderByTimeSlotAppointmentDateAscTimeSlotStartTimeAsc();
    List<Appointment> findByPatientIdOrderByTimeSlotAppointmentDateAscTimeSlotStartTimeAsc(Long patientId);
    List<Appointment> findByTimeSlotProviderIdOrderByTimeSlotAppointmentDateAscTimeSlotStartTimeAsc(Long providerId);

    boolean existsByTimeSlotId(Long timeSlotId);
}
