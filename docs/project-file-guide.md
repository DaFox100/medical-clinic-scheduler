# Project File Guide

This document describes the purpose of the main classes, templates, configuration files, tests, and reference files in this project.

## System Overview

This project is a Spring Boot medical clinic scheduling application with:

- Spring MVC controllers for page routing
- Thymeleaf templates for the frontend
- Spring Data JPA + Hibernate for persistence
- MySQL as the backing database
- Spring Security for login and role-based access
- Service classes that hold scheduling and booking business logic

The current primary roles are:

- `ADMIN`
- `PROVIDER`
- `PATIENT`

## Source Layout

### Application Entry Point

- [TermProj172Application.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/TermProj172Application.java)
  Starts the Spring Boot application.

## Configuration

- [AppLifecycleLogger.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/config/AppLifecycleLogger.java)
  Logs application startup and shutdown lifecycle events.

- [DataSeeder.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/config/DataSeeder.java)
  Seeds demo departments, providers, patients, time slots, and login accounts at startup. It now adds any missing demo accounts without requiring a fresh database.

- [RequestCorrelationFilter.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/config/RequestCorrelationFilter.java)
  Assigns correlation IDs to incoming requests so logs can be traced across a request flow.

- [RestClientConfig.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/config/RestClientConfig.java)
  Provides the `RestTemplate` bean used for internal/mock notification calls.

- [SecurityConfig.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/config/SecurityConfig.java)
  Configures Spring Security, login flow, route authorization, logout, and any exceptions for internal/mock endpoints.

- [SecurityEventLogger.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/config/SecurityEventLogger.java)
  Logs login success and failure events.

## Controllers

- [AppointmentBookingController.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/controllers/AppointmentBookingController.java)
  Serves the booking form and handles appointment booking submissions.

- [AppointmentRescheduleController.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/controllers/AppointmentRescheduleController.java)
  Serves the reschedule form and handles appointment rescheduling requests.

- [AppointmentViewerController.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/controllers/AppointmentViewerController.java)
  Shows appointments using a role-aware view. Patients see their own appointments, providers see appointments tied to their schedule, and admins see all appointments.

- [ConfirmationPageController.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/controllers/ConfirmationPageController.java)
  Serves the booking confirmation page after an appointment is created.

- [HealthController.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/controllers/HealthController.java)
  Exposes `/health` with application and database status information.

- [HomePageController.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/controllers/HomePageController.java)
  Serves the home schedule board with department and date filters. It passes role flags into the view so the page can adapt to patient, provider, and admin modes.

- [LoginController.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/controllers/LoginController.java)
  Serves the custom login page.

- [MockNotificationController.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/controllers/MockNotificationController.java)
  Simulates the notification service endpoint used during appointment confirmation.

- [TimeSlotController.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/controllers/TimeSlotController.java)
  Serves details for a specific configured time slot.

- [TimeSlotManagementController.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/controllers/TimeSlotManagementController.java)
  Serves and processes the time-slot management page. It supports prefilled slot creation from the home schedule.

## Domain Models and Form/DTO Classes

### Active Persistence Models

- [AppUser.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/domainModels/AppUser.java)
  Persistent application user record used for login and role mapping. Links a login to either a `Patient` or a `Provider`.

- [Appointment.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/domainModels/Appointment.java)
  Persistent appointment record. Represents a booked appointment tied to a patient and a unique time slot.

- [Department.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/domainModels/Department.java)
  Persistent clinic department such as Cardiology or Pediatrics.

- [Patient.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/domainModels/Patient.java)
  Persistent patient record used for booking and patient-scoped appointment views.

- [Provider.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/domainModels/Provider.java)
  Persistent provider record. Each provider belongs to one department.

- [TimeSlot.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/domainModels/TimeSlot.java)
  Persistent time slot record. Stores provider, date, start/end time, and slot status.

### Enums

- [AppUserRole.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/domainModels/AppUserRole.java)
  Role enum for `ADMIN`, `PROVIDER`, and `PATIENT`.

- [TimeSlotStatus.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/domainModels/TimeSlotStatus.java)
  Slot status enum. The active values are `AVAILABLE` and `BOOKED`.

### Form Objects and Request/Response DTOs

- [AppointmentBookingForm.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/domainModels/AppointmentBookingForm.java)
  Backing form object for appointment booking submissions.

- [AppointmentBookingResult.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/domainModels/AppointmentBookingResult.java)
  Service result wrapper that carries the saved appointment and notification response after booking.

- [AppointmentConfirmationDTO.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/domainModels/AppointmentConfirmationDTO.java)
  DTO sent to the mock notification service after appointment creation.

- [AppointmentRescheduleForm.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/domainModels/AppointmentRescheduleForm.java)
  Backing form object for the rescheduling UI.

- [NotificationResponse.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/domainModels/NotificationResponse.java)
  DTO representing the mock notification service response.

- [TimeSlotManagementForm.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/domainModels/TimeSlotManagementForm.java)
  Backing form object for create-slot requests on the management page.

## Repositories

- [AppUserRepository.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/repositories/AppUserRepository.java)
  Loads and stores application user accounts. Used heavily by authentication and seeding.

- [AppointmentRepository.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/repositories/AppointmentRepository.java)
  Handles appointment persistence and role-aware appointment queries.

- [DepartmentRepository.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/repositories/DepartmentRepository.java)
  Loads department data for scheduling and filters.

- [PatientRepository.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/repositories/PatientRepository.java)
  Loads patient records used in booking and user seeding.

- [ProviderRepository.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/repositories/ProviderRepository.java)
  Loads provider records, including department-scoped provider lists for provider-mode slot management.

- [TimeSlotRepository.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/repositories/TimeSlotRepository.java)
  Handles slot persistence, overlap checks, claim-if-available updates, and slot queries for the schedule board.

## Security

- [AppUserDetailsService.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/security/AppUserDetailsService.java)
  Bridges `AppUser` records into Spring Security’s `UserDetails` model during login.

## Services

- [AppointmentBookingService.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/services/AppointmentBookingService.java)
  Core booking business logic. Validates requests, claims available slots transactionally, persists appointments, prevents double booking, and triggers the mock confirmation service.

- [AppointmentRescheduleService.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/services/AppointmentRescheduleService.java)
  Handles rescheduling logic with access control and slot reassignment rules.

- [AppointmentService.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/services/AppointmentService.java)
  Provides role-aware appointment retrieval and appointment-view text for the appointments page.

- [ConfirmationPageService.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/services/ConfirmationPageService.java)
  Builds confirmation snapshots and sends them to the mock notification endpoint when invoked by the outbox dispatcher.

- [CurrentUserService.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/services/CurrentUserService.java)
  Resolves the authenticated `AppUser` and exposes helper methods like `isAdmin`, `isProvider`, and `isPatient`.

- [TimeSlotManagementService.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/services/TimeSlotManagementService.java)
  Handles slot creation, department-scoped provider access, overlap checks, slot status updates, and deletion rules.

- [TimeSlotService.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/services/TimeSlotService.java)
  Builds the home schedule board view model, loads slot details, and supports provider-mode empty-slot creation links.

## View Models

- [DepartmentScheduleView.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/viewmodels/DepartmentScheduleView.java)
  Represents one department section on the home schedule board.

- [HomeScheduleView.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/viewmodels/HomeScheduleView.java)
  Top-level view model for the home page schedule board.

- [ProviderScheduleRowView.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/viewmodels/ProviderScheduleRowView.java)
  Represents one time row in a provider’s schedule grid.

- [ProviderScheduleView.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/viewmodels/ProviderScheduleView.java)
  Represents one provider’s full grid inside a department section.

- [ScheduleCellView.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/java/com/example/termproj_172/viewmodels/ScheduleCellView.java)
  Represents one cell in the schedule board, including slot state and provider-mode empty-slot creation metadata.

## Templates

- [appointments.html](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/resources/templates/appointments.html)
  Displays appointments in a role-aware way.

- [bookAppointmentForm.html](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/resources/templates/bookAppointmentForm.html)
  Main booking form UI. It changes slightly depending on whether the current user is a patient or staff user.

- [bookingConfirmationPage.html](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/resources/templates/bookingConfirmationPage.html)
  Confirmation screen shown after a successful booking. Includes mock notification status details.

- [home.html](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/resources/templates/home.html)
  Main schedule board UI with department/date filters and provider-mode empty-slot actions.

- [login.html](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/resources/templates/login.html)
  Custom login page for Spring Security authentication.

- [manageTimeSlots.html](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/resources/templates/manageTimeSlots.html)
  Time-slot management UI for providers and admins. Also serves as the target when providers click empty schedule cells.

- [rescheduleAppointmentForm.html](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/resources/templates/rescheduleAppointmentForm.html)
  UI for rescheduling an existing appointment.

- [timeSlotDetails.html](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/resources/templates/timeSlotDetails.html)
  Shows details about a configured time slot and allows booking from available slots.

## Static Assets

- [styles.css](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/resources/static/css/styles.css)
  Main stylesheet for the application, including home schedule, forms, tables, role-aware layouts, and management screens.

## Runtime Configuration

- [application.properties](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/main/resources/application.properties)
  Defines application name, MySQL datasource settings, Hibernate/JPA settings, Thymeleaf settings, and error-message behavior.

Important settings in this file:

- `spring.datasource.*`
  Connects the app to MySQL.

- `spring.jpa.hibernate.ddl-auto=validate`
  Lets Hibernate update the schema to match entities.

- `spring.jpa.show-sql=true`
  Prints SQL during development.

- `spring.thymeleaf.cache=false`
  Disables template caching for easier UI iteration.

## Tests

- [TermProj172ApplicationTests.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/test/java/com/example/termproj_172/TermProj172ApplicationTests.java)
  Basic Spring Boot context-load test.

- [AppointmentBookingServiceTest.java](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/test/java/com/example/termproj_172/services/AppointmentBookingServiceTest.java)
  Unit tests for booking logic, especially double-booking protection and persistence collision handling.

- [org.mockito.plugins.MockMaker](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker)
  Mockito test configuration file that controls mock creation strategy for the local Java runtime.

## Context / Planning Documents

These files are reference material and not application runtime code:

- [CMPE172_M4_Concurrency_Michael_Fox.pdf](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/contextual_files/CMPE172_M4_Concurrency_Michael_Fox.pdf)
  Concurrency and double-booking guidance.

- [CMPE172_M6_SysMGMT_Michael_Fox.pdf](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/contextual_files/CMPE172_M6_SysMGMT_Michael_Fox.pdf)
  System management guidance including health/logging direction.

- [M3_DesignNotes.pdf](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/contextual_files/M3_DesignNotes.pdf)
  Design notes for the project’s intended architecture and roles.

- [Relational Schema and Explanation.pdf](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/contextual_files/Relational Schema and Explanation.pdf)
  Database modeling guidance.

- [TermProject_M5_report.pdf](/Users/michaelfox/Documents/SJSU/CMPE172/TermProj_172/contextual_files/TermProject_M5_report.pdf)
  Notification/service-integration reference for the mock service call.

## Active Flow Summary

The most important active runtime path is:

1. User logs in through `SecurityConfig`, `LoginController`, and `AppUserDetailsService`.
2. Home schedule loads through `HomePageController` and `TimeSlotService`.
3. Booking/rescheduling flows go through their controllers into `AppointmentBookingService` or `AppointmentRescheduleService`.
4. Persistence happens through the JPA repositories with Hibernate underneath.
5. Confirmation data is sent through `ConfirmationPageService` to `MockNotificationController`.
6. Health and logging support system visibility through `HealthController` and the logging/config classes.

## Notes

The current application is centered on `AppUser`, `Patient`, `Provider`, `Department`, `TimeSlot`, and `Appointment`.
