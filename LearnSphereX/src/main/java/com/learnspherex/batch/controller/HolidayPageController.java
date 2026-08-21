package com.learnspherex.batch.controller;

import com.learnspherex.batch.entity.Holiday;
import com.learnspherex.batch.entity.HolidayType;
import com.learnspherex.batch.service.BatchService;
import com.learnspherex.batch.service.HolidayService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HolidayPageController {

    private final HolidayService holidayService;
    private final BatchService batchService;

    public HolidayPageController(HolidayService holidayService, BatchService batchService) {
        this.holidayService = holidayService;
        this.batchService = batchService;
    }

    @GetMapping("/holidays")
    public String list(Model model) {
        model.addAttribute("holidays", holidayService.getAllHolidays());
        model.addAttribute("batches", batchService.getAllBatches());
        model.addAttribute("holidayTypes", HolidayType.values());
        return "holidays";
    }

    @PostMapping("/holidays")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public String create(@RequestParam String holidayDate, @RequestParam String reason,
                          @RequestParam HolidayType holidayType,
                          @RequestParam(required = false) Long batchId) {
        Holiday h = new Holiday();
        h.setHolidayDate(java.time.LocalDate.parse(holidayDate));
        h.setReason(reason);
        h.setHolidayType(holidayType);
        h.setBatchId(batchId);
        holidayService.createHoliday(h);
        return "redirect:/holidays?created";
    }

    @PostMapping("/holidays/{id}/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public String delete(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return "redirect:/holidays";
    }
}
