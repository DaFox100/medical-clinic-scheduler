package com.example.termproj_172.services;

import com.example.termproj_172.domainModels.Appointment;
import com.example.termproj_172.domainModels.AppointmentBookingForm;
import com.example.termproj_172.domainModels.AppointmentBookingResult;
import com.example.termproj_172.domainModels.AppUser;
import com.example.termproj_172.domainModels.AppUserRole;
import com.example.termproj_172.domainModels.Department;
import com.example.termproj_172.domainModels.Patient;
import com.example.termproj_172.domainModels.Provider;
import com.example.termproj_172.domainModels.TimeSlot;
import com.example.termproj_172.domainModels.TimeSlotStatus;
import com.example.termproj_172.repositories.AppointmentRepository;
import com.example.termproj_172.repositories.PatientRepository;
import com.example.termproj_172.repositories.TimeSlotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentBookingServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private NotificationOutbox notificationOutbox;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AppointmentBookingService appointmentBookingService;

    @Test
    void rejectsBookingWhenSlotClaimFails() {
        AppointmentBookingForm form = buildForm();
        Patient patient = buildPatient();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(timeSlotRepository.claimSlotIfAvailable(10L, TimeSlotStatus.AVAILABLE, TimeSlotStatus.BOOKED)).thenReturn(0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentBookingService.bookAppointment(form, buildAdminUser())
        );

        assertEquals("That time slot was just booked. Choose another slot.", exception.getMessage());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void convertsUniqueConstraintCollisionIntoUserFacingBookingError() {
        AppointmentBookingForm form = buildForm();
        Patient patient = buildPatient();
        TimeSlot timeSlot = buildTimeSlot();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(timeSlotRepository.claimSlotIfAvailable(10L, TimeSlotStatus.AVAILABLE, TimeSlotStatus.BOOKED)).thenReturn(1);
        when(timeSlotRepository.findById(10L)).thenReturn(Optional.of(timeSlot));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentBookingService.bookAppointment(form, buildAdminUser())
        );

        assertEquals("That time slot was just booked. Choose another slot.", exception.getMessage());
        verify(timeSlotRepository).claimSlotIfAvailable(10L, TimeSlotStatus.AVAILABLE, TimeSlotStatus.BOOKED);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void queuesNotificationWhenBookingSucceeds() {
        AppointmentBookingForm form = buildForm();
        Patient patient = buildPatient();
        TimeSlot timeSlot = buildTimeSlot();
        Appointment savedAppointment = new Appointment();
        savedAppointment.setId(99L);
        savedAppointment.setPatient(patient);
        savedAppointment.setTimeSlot(timeSlot);
        savedAppointment.setDescription("Follow up");
        savedAppointment.setStatus("Scheduled");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(timeSlotRepository.claimSlotIfAvailable(10L, TimeSlotStatus.AVAILABLE, TimeSlotStatus.BOOKED)).thenReturn(1);
        when(timeSlotRepository.findById(10L)).thenReturn(Optional.of(timeSlot));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);


        AppointmentBookingResult result = appointmentBookingService.bookAppointment(form, buildAdminUser());

        assertEquals(99L, result.getAppointment().getId());
        assertEquals("QUEUED", result.getNotificationResponse().getStatus());
        verify(notificationOutbox).enqueue(savedAppointment);
    }

    private AppointmentBookingForm buildForm() {
        AppointmentBookingForm form = new AppointmentBookingForm();
        form.setPatientId(1L);
        form.setTimeSlotId(10L);
        form.setDescription("Follow up");
        return form;
    }

    private Patient buildPatient() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFullName("Jane Doe");
        patient.setEmail("jane@example.com");
        return patient;
    }

    private TimeSlot buildTimeSlot() {
        Department department = new Department();
        department.setId(3L);
        department.setName("Cardiology");

        Provider provider = new Provider();
        provider.setId(2L);
        provider.setFullName("Dr. Patel");
        provider.setDepartment(department);

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId(10L);
        timeSlot.setProvider(provider);
        timeSlot.setAppointmentDate(LocalDate.of(2026, 5, 9));
        timeSlot.setStartTime(LocalTime.of(9, 0));
        timeSlot.setEndTime(LocalTime.of(9, 30));
        timeSlot.setStatus(TimeSlotStatus.BOOKED);
        return timeSlot;
    }

    private AppUser buildAdminUser() {
        AppUser appUser = new AppUser();
        appUser.setId(50L);
        appUser.setUsername("admin");
        appUser.setRole(AppUserRole.ADMIN);
        return appUser;
    }
}
