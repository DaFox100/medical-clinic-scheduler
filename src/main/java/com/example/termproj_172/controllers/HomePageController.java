package com.example.termproj_172.controllers;

import com.example.termproj_172.services.TimeSlotService;
import com.example.termproj_172.services.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class HomePageController {

    private final TimeSlotService timeSlotService;
    private final CurrentUserService currentUserService;

    public HomePageController(TimeSlotService timeSlotService,
                              CurrentUserService currentUserService) {
        this.timeSlotService = timeSlotService;
        this.currentUserService = currentUserService;
    }

    @GetMapping({"/","HomePage"})
    public String home(@RequestParam(name = "department", required = false) String department,
                       @RequestParam(name = "startDate", required = false) LocalDate startDate,
                       @RequestParam(name = "endDate", required = false) LocalDate endDate,
                       Authentication authentication,
                       Model model) {
        var user = currentUserService.getRequiredUser(authentication);
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now();
        LocalDate effectiveEndDate = endDate != null ? endDate : effectiveStartDate.plusDays(4);

        if (effectiveEndDate.isBefore(effectiveStartDate)) {
            effectiveEndDate = effectiveStartDate.plusDays(4);
        }

        if (effectiveStartDate.plusDays(13).isBefore(effectiveEndDate)) {
            effectiveEndDate = effectiveStartDate.plusDays(13);
        }

        model.addAttribute("scheduleView", timeSlotService.buildHomeSchedule(department, effectiveStartDate, effectiveEndDate, user));
        model.addAttribute("departments", timeSlotService.getDepartmentNames());
        model.addAttribute("selectedDepartment", department == null ? "" : department);
        model.addAttribute("selectedStartDate", effectiveStartDate);
        model.addAttribute("selectedEndDate", effectiveEndDate);
        model.addAttribute("isAdminUser", currentUserService.isAdmin(user));
        model.addAttribute("isProviderUser", currentUserService.isProvider(user));
        model.addAttribute("isPatientUser", currentUserService.isPatient(user));
        return "home";
    }
}
