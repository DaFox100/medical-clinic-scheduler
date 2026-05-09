package com.example.termproj_172.controllers;

import com.example.termproj_172.domainModels.TimeSlotManagementForm;
import com.example.termproj_172.domainModels.TimeSlotStatus;
import com.example.termproj_172.services.CurrentUserService;
import com.example.termproj_172.services.TimeSlotManagementService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
public class TimeSlotManagementController {

    private final TimeSlotManagementService timeSlotManagementService;
    private final CurrentUserService currentUserService;

    public TimeSlotManagementController(TimeSlotManagementService timeSlotManagementService,
                                        CurrentUserService currentUserService) {
        this.timeSlotManagementService = timeSlotManagementService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/time-slots/manage")
    public String manageTimeSlots(@RequestParam(name = "providerId", required = false) Long providerId,
                                  @RequestParam(name = "appointmentDate", required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate,
                                  @RequestParam(name = "startTime", required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                  @RequestParam(name = "endTime", required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
                                  Model model,
                                  Authentication authentication) {
        var user = currentUserService.getRequiredUser(authentication);
        if (!model.containsAttribute("timeSlotForm")) {
            model.addAttribute("timeSlotForm",
                    timeSlotManagementService.buildPrefilledForm(user, providerId, appointmentDate, startTime, endTime));
        }
        populateModel(model, user);
        model.addAttribute("prefilledFromSchedule",
                providerId != null && appointmentDate != null && startTime != null && endTime != null);
        model.addAttribute("isProviderUser", currentUserService.isProvider(user));
        model.addAttribute("currentProvider", user.getProvider());
        return "manageTimeSlots";
    }

    @PostMapping("/time-slots/manage")
    public String createTimeSlot(@ModelAttribute("timeSlotForm") TimeSlotManagementForm timeSlotForm,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        var user = currentUserService.getRequiredUser(authentication);
        try {
            timeSlotManagementService.createTimeSlot(timeSlotForm, user);
            redirectAttributes.addFlashAttribute("successMessage", "Time slot created successfully.");
            return "redirect:/time-slots/manage";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            populateModel(model, user);
            model.addAttribute("prefilledFromSchedule",
                    timeSlotForm.getProviderId() != null
                            && timeSlotForm.getAppointmentDate() != null
                            && timeSlotForm.getStartTime() != null
                            && timeSlotForm.getEndTime() != null);
            model.addAttribute("isProviderUser", currentUserService.isProvider(user));
            model.addAttribute("currentProvider", user.getProvider());
            return "manageTimeSlots";
        }
    }

    @PostMapping("/time-slots/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam("status") TimeSlotStatus status,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        var user = currentUserService.getRequiredUser(authentication);
        try {
            timeSlotManagementService.updateStatus(id, status, user);
            redirectAttributes.addFlashAttribute("successMessage", "Time slot status updated.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/time-slots/manage";
    }

    @PostMapping("/time-slots/{id}/delete")
    public String deleteTimeSlot(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        var user = currentUserService.getRequiredUser(authentication);
        try {
            timeSlotManagementService.deleteUnusedSlot(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Time slot deleted.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/time-slots/manage";
    }

    private void populateModel(Model model, com.example.termproj_172.domainModels.AppUser user) {
        model.addAttribute("providers", timeSlotManagementService.getVisibleProviders(user));
        model.addAttribute("timeSlots", timeSlotManagementService.getVisibleTimeSlots(user));
        model.addAttribute("statuses", TimeSlotStatus.values());
    }
}
