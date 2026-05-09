package com.example.termproj_172.controllers;

import com.example.termproj_172.services.AppointmentService;
import com.example.termproj_172.services.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppointmentViewerController {

    private final AppointmentService appointmentService;
    private final CurrentUserService currentUserService;

    public AppointmentViewerController(AppointmentService appointmentService,
                                       CurrentUserService currentUserService) {
        this.appointmentService = appointmentService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/appointments")
    public String viewAppointments(Model model, Authentication authentication) {
        var user = currentUserService.getRequiredUser(authentication);
        model.addAttribute("appointments", appointmentService.getVisibleAppointments(user));
        model.addAttribute("appointmentViewTitle", appointmentService.getAppointmentViewTitle(user));
        model.addAttribute("appointmentViewCopy", appointmentService.getAppointmentViewCopy(user));
        model.addAttribute("isPatientUser", currentUserService.isPatient(user));
        return "appointments";
    }
}
