package com.learnspherex.batch.controller;


import jakarta.validation.Valid;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.learnspherex.batch.entity.BatchSchedule;
import com.learnspherex.batch.service.BatchScheduleService;

@RestController
@RequestMapping("/api/batch-schedules")
public class BatchScheduleController {

    private final BatchScheduleService scheduleService;

    public BatchScheduleController(BatchScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ResponseEntity<BatchSchedule> createSchedule(
            @Valid @RequestBody BatchSchedule schedule) {

        return ResponseEntity.ok(
                scheduleService.createSchedule(schedule));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BatchSchedule> getScheduleById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                scheduleService.getScheduleById(id));
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<List<BatchSchedule>> getSchedulesByBatchId(
            @PathVariable Long batchId) {

        return ResponseEntity.ok(
                scheduleService.getSchedulesByBatchId(batchId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BatchSchedule> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody BatchSchedule schedule) {

        return ResponseEntity.ok(
                scheduleService.updateSchedule(id, schedule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long id) {

        scheduleService.deleteSchedule(id);

        return ResponseEntity.noContent().build();
    }
}