package com.learnspherex.batch.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learnspherex.batch.entity.Attendance;
import com.learnspherex.batch.service.AttendanceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(
            AttendanceService attendanceService) {

        this.attendanceService = attendanceService;
    }

    // ==========================================
    // MARK ATTENDANCE
    // ==========================================

    @PostMapping
    public ResponseEntity<Attendance> markAttendance(
            @Valid @RequestBody Attendance attendance,
            Authentication authentication) {

        Attendance savedAttendance =
                attendanceService.markAttendance(
                        attendance, authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedAttendance);
    }

    // ==========================================
    // GET ALL ATTENDANCE
    // ==========================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Attendance>>
    getAllAttendance() {

        return ResponseEntity.ok(
                attendanceService.getAllAttendance()
        );
    }

    // ==========================================
    // GET ATTENDANCE BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<Attendance> getAttendanceById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService
                        .getAttendanceById(id, authentication)
        );
    }

    // ==========================================
    // GET ATTENDANCE BY BATCH
    // ==========================================

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<List<Attendance>>
    getAttendanceByBatchId(
            @PathVariable Long batchId,
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService
                        .getAttendanceByBatchId(batchId, authentication)
        );
    }

    // ==========================================
    // GET ATTENDANCE BY STUDENT
    // ==========================================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Attendance>>
    getAttendanceByStudentId(
            @PathVariable Long studentId,
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService
                        .getAttendanceByStudentId(
                                studentId, authentication)
        );
    }

    // ==========================================
    // GET ATTENDANCE BY BATCH + STUDENT
    // ==========================================

    @GetMapping("/batch/{batchId}/student/{studentId}")
    public ResponseEntity<List<Attendance>>
    getAttendanceByBatchAndStudent(
            @PathVariable Long batchId,
            @PathVariable Long studentId,
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService
                        .getAttendanceByBatchAndStudent(
                                batchId,
                                studentId,
                                authentication)
        );
    }

    // ==========================================
    // ATTENDANCE PERCENTAGE
    // ==========================================

    @GetMapping(
            "/batch/{batchId}/student/{studentId}/percentage")
    public ResponseEntity<Double>
    calculateAttendancePercentage(
            @PathVariable Long batchId,
            @PathVariable Long studentId,
            Authentication authentication) {

        double percentage =
                attendanceService
                        .calculateAttendancePercentage(
                                batchId,
                                studentId,
                                authentication);

        return ResponseEntity.ok(percentage);
    }

    // ==========================================
    // MONTHLY ATTENDANCE
    // ==========================================

    @GetMapping("/student/{studentId}/monthly")
    public ResponseEntity<List<Attendance>>
    getMonthlyAttendance(
            @PathVariable Long studentId,
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService
                        .getMonthlyAttendance(
                                studentId,
                                year,
                                month,
                                authentication)
        );
    }

    // ==========================================
    // BATCH ATTENDANCE REPORT
    // ==========================================

    @GetMapping("/batch/{batchId}/report")
    public ResponseEntity<List<Attendance>>
    getBatchAttendanceReport(
            @PathVariable Long batchId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService
                        .getBatchAttendanceReport(
                                batchId,
                                startDate,
                                endDate,
                                authentication)
        );
    }

    // ==========================================
    // LOW ATTENDANCE STUDENTS
    // ==========================================

    @GetMapping("/batch/{batchId}/low")
    public ResponseEntity<Map<Long, Double>>
    getLowAttendanceStudents(
            @PathVariable Long batchId,
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService
                        .getLowAttendanceStudents(
                                batchId, authentication)
        );
    }

    // ==========================================
    // UPDATE ATTENDANCE
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<Attendance>
    updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody Attendance attendance,
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.updateAttendance(
                        id,
                        attendance,
                        authentication)
        );
    }

    // ==========================================
    // DELETE ATTENDANCE
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendance(
            @PathVariable Long id,
            Authentication authentication) {

        attendanceService.deleteAttendance(id, authentication);

        return ResponseEntity
                .noContent()
                .build();
    }
}