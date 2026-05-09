package com.example.termproj_172.controllers;

import com.example.termproj_172.domainModels.TimeSlot;
import com.example.termproj_172.services.TimeSlotService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @GetMapping("/time-slots/{id}")
    public String viewTimeSlot(@PathVariable Long id, Model model) {
        TimeSlot timeSlot = timeSlotService.getTimeSlot(id);
        model.addAttribute("timeSlot", timeSlot);
        model.addAttribute("canBook", timeSlot.isAvailable());
        return "timeSlotDetails";
    }
}
