package com.learnspherex.batch.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.learnspherex.batch.entity.BatchSchedule;
import com.learnspherex.batch.service.BatchScheduleService;

@Controller
public class BatchSchedulePageController {

    private final BatchScheduleService scheduleService;

    public BatchSchedulePageController(
            BatchScheduleService scheduleService) {

        this.scheduleService = scheduleService;
    }

    // =========================
    // SHOW SCHEDULES BY BATCH
    // =========================

    @GetMapping("/batch-schedules/batch/{batchId}")
    public String showSchedulesByBatch(
            @PathVariable Long batchId,
            Model model) {

        model.addAttribute(
                "schedules",
                scheduleService.getSchedulesByBatchId(batchId));

        model.addAttribute("batchId", batchId);

        return "batch-schedules";
    }


    // =========================
    // SHOW CREATE FORM
    // =========================

    @GetMapping("/batch-schedules/new")
    public String showCreateScheduleForm(
            @org.springframework.web.bind.annotation.RequestParam Long batchId,
            Model model) {

        BatchSchedule schedule = new BatchSchedule();

        schedule.setBatchId(batchId);

        model.addAttribute("schedule", schedule);

        return "batch-schedule-form";
    }


    // =========================
    // CREATE SCHEDULE
    // =========================

    @PostMapping("/batch-schedules")
    public String createSchedule(
            @ModelAttribute BatchSchedule schedule) {

        scheduleService.createSchedule(schedule);

        return "redirect:/batch-schedules/batch/"
                + schedule.getBatchId();
    }


    // =========================
    // SHOW DETAILS
    // =========================

    @GetMapping("/batch-schedules/{id}")
    public String showScheduleDetails(
            @PathVariable Long id,
            Model model) {

        BatchSchedule schedule =
                scheduleService.getScheduleById(id);

        model.addAttribute("schedule", schedule);

        return "batch-schedule-details";
    }


    // =========================
    // SHOW EDIT FORM
    // =========================

    @GetMapping("/batch-schedules/{id}/edit")
    public String showEditScheduleForm(
            @PathVariable Long id,
            Model model) {

        BatchSchedule schedule =
                scheduleService.getScheduleById(id);

        model.addAttribute("schedule", schedule);

        return "batch-schedule-form";
    }


    // =========================
    // UPDATE
    // =========================

    @PostMapping("/batch-schedules/{id}")
    public String updateSchedule(
            @PathVariable Long id,
            @ModelAttribute BatchSchedule schedule) {

        scheduleService.updateSchedule(id, schedule);

        return "redirect:/batch-schedules/batch/"
                + schedule.getBatchId();
    }


    // =========================
    // DELETE
    // =========================

    @PostMapping("/batch-schedules/{id}/delete")
    public String deleteSchedule(
            @PathVariable Long id) {

        BatchSchedule schedule =
                scheduleService.getScheduleById(id);

        Long batchId = schedule.getBatchId();

        scheduleService.deleteSchedule(id);

        return "redirect:/batch-schedules/batch/"
                + batchId;
    }
}