package com.learnspherex.batch.controller;


import jakarta.validation.Valid;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.learnspherex.batch.entity.BatchEnrollment;
import com.learnspherex.batch.service.BatchEnrollmentService;

@RestController
@RequestMapping("/api/batch-enrollments")
public class BatchEnrollmentController {

    private final BatchEnrollmentService enrollmentService;

    public BatchEnrollmentController(
            BatchEnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<BatchEnrollment> enrollStudent(
            @Valid @RequestBody BatchEnrollment enrollment) {

        return ResponseEntity.ok(
                enrollmentService.enrollStudent(enrollment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BatchEnrollment> getEnrollmentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                enrollmentService.getEnrollmentById(id));
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<List<BatchEnrollment>> getByBatch(
            @PathVariable Long batchId) {

        return ResponseEntity.ok(
                enrollmentService.getEnrollmentsByBatchId(batchId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<BatchEnrollment>> getByStudent(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                enrollmentService.getEnrollmentsByStudentId(studentId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BatchEnrollment> updateEnrollment(
            @PathVariable Long id,
            @Valid @RequestBody BatchEnrollment enrollment) {

        return ResponseEntity.ok(
                enrollmentService.updateEnrollment(id, enrollment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(
            @PathVariable Long id) {

        enrollmentService.deleteEnrollment(id);

        return ResponseEntity.noContent().build();
    }
}