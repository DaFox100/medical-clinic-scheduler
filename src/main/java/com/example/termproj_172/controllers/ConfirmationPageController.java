package com.example.termproj_172.controllers;

import com.example.termproj_172.services.AppointmentService;
import com.example.termproj_172.services.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ConfirmationPageController {

    private final AppointmentService appointmentService;
    private final CurrentUserService currentUserService;

    public ConfirmationPageController(AppointmentService appointmentService,
                                      CurrentUserService currentUserService) {
        this.appointmentService = appointmentService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/appointments/{id}/confirmation")
    public String showConfirmationPage(@PathVariable Long id, Authentication authentication, Model model) {
        var user = currentUserService.getRequiredUser(authentication);
        model.addAttribute("appointment", appointmentService.getAccessibleAppointment(id, user));
        return "bookingConfirmationPage";
    }
}
