package com.example.termproj_172.controllers;

import com.example.termproj_172.domainModels.AppointmentRescheduleForm;
import com.example.termproj_172.services.AppointmentRescheduleService;
import com.example.termproj_172.services.AppointmentService;
import com.example.termproj_172.services.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AppointmentRescheduleController {

    private final AppointmentService appointmentService;
    private final AppointmentRescheduleService appointmentRescheduleService;
    private final CurrentUserService currentUserService;

    public AppointmentRescheduleController(AppointmentService appointmentService,
                                           AppointmentRescheduleService appointmentRescheduleService,
                                           CurrentUserService currentUserService) {
        this.appointmentService = appointmentService;
        this.appointmentRescheduleService = appointmentRescheduleService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/appointments/{id}/reschedule")
    public String rescheduleForm(@PathVariable Long id, Authentication authentication, Model model) {
        var user = currentUserService.getRequiredUser(authentication);
        if (!model.containsAttribute("rescheduleForm")) {
            model.addAttribute("rescheduleForm", new AppointmentRescheduleForm());
        }

        var appointment = appointmentService.getAccessibleAppointment(id, user);
        model.addAttribute("appointment", appointment);
        model.addAttribute("availableTimeSlots",
                appointmentRescheduleService.getAvailableTimeSlotsForReschedule(appointment.getTimeSlot().getId()));
        return "rescheduleAppointmentForm";
    }

    @PostMapping("/appointments/{id}/reschedule")
    public String reschedule(@PathVariable Long id,
                             @ModelAttribute("rescheduleForm") AppointmentRescheduleForm rescheduleForm,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        var user = currentUserService.getRequiredUser(authentication);
        try {
            appointmentRescheduleService.rescheduleAppointment(id, rescheduleForm.getTimeSlotId(), user);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment rescheduled successfully.");
            return "redirect:/appointments/" + id + "/confirmation";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            var appointment = appointmentService.getAccessibleAppointment(id, user);
            model.addAttribute("appointment", appointment);
            model.addAttribute("availableTimeSlots",
                    appointmentRescheduleService.getAvailableTimeSlotsForReschedule(appointment.getTimeSlot().getId()));
            return "rescheduleAppointmentForm";
        }
    }
}
