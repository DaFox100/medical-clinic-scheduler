package com.example.termproj_172.controllers;

import com.example.termproj_172.domainModels.Appointment;
import com.example.termproj_172.domainModels.AppointmentBookingForm;
import com.example.termproj_172.domainModels.AppointmentBookingResult;
import com.example.termproj_172.domainModels.TimeSlot;
import com.example.termproj_172.services.AppointmentBookingService;
import com.example.termproj_172.services.AppointmentService;
import com.example.termproj_172.services.CurrentUserService;
import com.example.termproj_172.services.TimeSlotService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AppointmentBookingController {

    private final AppointmentBookingService appointmentBookingService;
    private final AppointmentService appointmentService;
    private final CurrentUserService currentUserService;
    private final TimeSlotService timeSlotService;

    public AppointmentBookingController(AppointmentBookingService appointmentBookingService,
                                        AppointmentService appointmentService,
                                        CurrentUserService currentUserService,
                                        TimeSlotService timeSlotService) {
        this.appointmentBookingService = appointmentBookingService;
        this.appointmentService = appointmentService;
        this.currentUserService = currentUserService;
        this.timeSlotService = timeSlotService;
    }

    @GetMapping("/appointments/book")
    public String bookAppointmentForm(@RequestParam(name = "timeSlotId", required = false) Long timeSlotId,
                                      Authentication authentication,
                                      Model model) {
        var user = currentUserService.getRequiredUser(authentication);
        if (!model.containsAttribute("bookingForm")) {
            AppointmentBookingForm bookingForm = new AppointmentBookingForm();
            if (currentUserService.isPatient(user) && user.getPatient() != null) {
                bookingForm.setPatientId(user.getPatient().getId());
            }
            if (timeSlotId != null) {
                TimeSlot selectedTimeSlot = timeSlotService.getAvailableTimeSlot(timeSlotId);
                bookingForm.setTimeSlotId(selectedTimeSlot.getId());
                model.addAttribute("selectedTimeSlot", selectedTimeSlot);
            }
            model.addAttribute("bookingForm", bookingForm);
        }
        populateBookingOptions(model, user);
        addSelectedTimeSlot(model);
        model.addAttribute("isPatientUser", currentUserService.isPatient(user));
        model.addAttribute("currentPatient", user.getPatient());
        return "bookAppointmentForm";
    }

    @PostMapping("/appointments/book")
    public String bookAppointment(@ModelAttribute("bookingForm") AppointmentBookingForm bookingForm,
                                  Authentication authentication,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        var user = currentUserService.getRequiredUser(authentication);
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please correct the form and submit again.");
            populateBookingOptions(model, user);
            addSelectedTimeSlot(model);
            model.addAttribute("isPatientUser", currentUserService.isPatient(user));
            model.addAttribute("currentPatient", user.getPatient());
            return "bookAppointmentForm";
        }

        try {
            AppointmentBookingResult bookingResult = appointmentBookingService.bookAppointment(bookingForm, user);
            Appointment savedAppointment = bookingResult.getAppointment();
            redirectAttributes.addFlashAttribute("successMessage", "Appointment booked successfully.");
            redirectAttributes.addFlashAttribute("notificationResponse", bookingResult.getNotificationResponse());
            return "redirect:/appointments/" + savedAppointment.getId() + "/confirmation";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            populateBookingOptions(model, user);
            addSelectedTimeSlot(model);
            model.addAttribute("isPatientUser", currentUserService.isPatient(user));
            model.addAttribute("currentPatient", user.getPatient());
            return "bookAppointmentForm";
        }
    }

    private void populateBookingOptions(Model model, com.example.termproj_172.domainModels.AppUser user) {
        model.addAttribute("patients", appointmentService.getVisiblePatientsForBooking(user));
        model.addAttribute("availableTimeSlots", appointmentBookingService.getAvailableTimeSlots());
    }

    private void addSelectedTimeSlot(Model model) {
        if (model.containsAttribute("selectedTimeSlot")) {
            return;
        }

        Object bookingFormObject = model.asMap().get("bookingForm");
        if (!(bookingFormObject instanceof AppointmentBookingForm bookingForm)) {
            return;
        }

        if (bookingForm.getTimeSlotId() == null) {
            return;
        }

        try {
            model.addAttribute("selectedTimeSlot", timeSlotService.getAvailableTimeSlot(bookingForm.getTimeSlotId()));
        } catch (IllegalArgumentException ignored) {
            // The slot may have become unavailable after the form was loaded.
        }
    }
}
