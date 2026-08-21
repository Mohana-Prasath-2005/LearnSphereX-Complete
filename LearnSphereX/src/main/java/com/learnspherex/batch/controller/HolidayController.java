package com.learnspherex.batch.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnspherex.batch.entity.Holiday;
import com.learnspherex.batch.service.HolidayService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(
            HolidayService holidayService) {

        this.holidayService = holidayService;
    }

    // ==========================================
    // CREATE HOLIDAY
    // ==========================================

    @PostMapping
    public ResponseEntity<Holiday> createHoliday(
            @Valid @RequestBody Holiday holiday) {

        Holiday savedHoliday =
                holidayService.createHoliday(holiday);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedHoliday);
    }

    // ==========================================
    // GET ALL HOLIDAYS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<Holiday>> getAllHolidays() {

        return ResponseEntity.ok(
                holidayService.getAllHolidays()
        );
    }

    // ==========================================
    // GET BATCH-SPECIFIC HOLIDAYS
    // ==========================================

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<List<Holiday>> getHolidaysByBatchId(
            @PathVariable Long batchId) {

        return ResponseEntity.ok(
                holidayService.getHolidaysByBatchId(
                        batchId
                )
        );
    }

    // ==========================================
    // GET ALL APPLICABLE HOLIDAYS FOR A BATCH
    // ==========================================

    @GetMapping("/batch/{batchId}/applicable")
    public ResponseEntity<List<Holiday>>
    getApplicableHolidaysByBatchId(
            @PathVariable Long batchId) {

        return ResponseEntity.ok(
                holidayService
                        .getApplicableHolidaysByBatchId(
                                batchId
                        )
        );
    }

    // ==========================================
    // GET HOLIDAY BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<Holiday> getHolidayById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                holidayService.getHolidayById(id)
        );
    }

    // ==========================================
    // UPDATE HOLIDAY
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<Holiday> updateHoliday(
            @PathVariable Long id,
            @Valid @RequestBody Holiday holiday) {

        return ResponseEntity.ok(
                holidayService.updateHoliday(
                        id,
                        holiday
                )
        );
    }

    // ==========================================
    // DELETE HOLIDAY
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoliday(
            @PathVariable Long id) {

        holidayService.deleteHoliday(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}