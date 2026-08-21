package com.learnspherex.batch.controller;

import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.learnspherex.batch.entity.Attendance;
import com.learnspherex.batch.service.AttendanceService;

@Controller
public class AttendancePageController {

    private final AttendanceService attendanceService;

    public AttendancePageController(
            AttendanceService attendanceService) {

        this.attendanceService = attendanceService;
    }

    // ==========================================
    // SHOW ALL ATTENDANCE
    // ==========================================

    @GetMapping("/attendance")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAllAttendance(Model model) {

        model.addAttribute(
                "attendances",
                attendanceService.getAllAttendance());

        return "attendances";
    }

    // ==========================================
    // SHOW ATTENDANCE BY BATCH
    // ==========================================

    @GetMapping("/attendance/batch/{batchId}")
    public String showAttendanceByBatch(
            @PathVariable Long batchId,
            Model model,
            Authentication authentication) {

        model.addAttribute(
                "attendances",
                attendanceService
                        .getAttendanceByBatchId(batchId, authentication));

        model.addAttribute(
                "batchId",
                batchId);

        return "attendances";
    }

    // ==========================================
    // SHOW MONTHLY ATTENDANCE (student-wise)
    // ==========================================

    @GetMapping("/attendance/student/{studentId}/monthly")
    public String showMonthlyAttendance(
            @PathVariable Long studentId,
            @RequestParam int year,
            @RequestParam int month,
            Model model,
            Authentication authentication) {

        model.addAttribute(
                "attendances",
                attendanceService.getMonthlyAttendance(studentId, year, month, authentication));

        model.addAttribute("studentId", studentId);
        model.addAttribute("pageTitle", "Monthly Attendance - " + year + "/" + month);

        return "attendances";
    }

    // ==========================================
    // SHOW BATCH ATTENDANCE REPORT
    // ==========================================

    @GetMapping("/attendance/batch/{batchId}/report")
    public String showBatchReport(
            @PathVariable Long batchId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Model model,
            Authentication authentication) {

        model.addAttribute(
                "attendances",
                attendanceService.getBatchAttendanceReport(batchId, startDate, endDate, authentication));

        model.addAttribute("batchId", batchId);
        model.addAttribute("pageTitle", "Attendance Report (" + startDate + " to " + endDate + ")");

        return "attendances";
    }

    // ==========================================
    // SHOW LOW-ATTENDANCE STUDENTS FOR A BATCH
    // ==========================================

    @GetMapping("/attendance/batch/{batchId}/low")
    public String showLowAttendance(
            @PathVariable Long batchId,
            Model model,
            Authentication authentication) {

        model.addAttribute(
                "lowAttendance",
                attendanceService.getLowAttendanceStudents(batchId, authentication));

        model.addAttribute("batchId", batchId);

        return "low-attendance";
    }

    // ==========================================
    // SHOW CREATE FORM
    // ==========================================

    @GetMapping("/attendance/new")
    public String showCreateForm(Model model) {

        model.addAttribute(
                "attendance",
                new Attendance());

        return "attendance-form";
    }

    // ==========================================
    // CREATE / MARK ATTENDANCE
    // ==========================================

    @PostMapping("/attendance")
    public String createAttendance(
            @ModelAttribute Attendance attendance,
            Authentication authentication) {

        attendanceService.markAttendance(attendance, authentication);

        return "redirect:/attendance";
    }

    // ==========================================
    // SHOW ATTENDANCE DETAILS
    // ==========================================

    @GetMapping("/attendance/{id}")
    public String showAttendanceDetails(
            @PathVariable Long id,
            Model model,
            Authentication authentication) {

        Attendance attendance =
                attendanceService.getAttendanceById(id, authentication);

        model.addAttribute(
                "attendance",
                attendance);

        return "attendance-details";
    }

    // ==========================================
    // SHOW EDIT FORM
    // ==========================================

    @GetMapping("/attendance/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model,
            Authentication authentication) {

        Attendance attendance =
                attendanceService.getAttendanceById(id, authentication);

        model.addAttribute(
                "attendance",
                attendance);

        return "attendance-form";
    }

    // ==========================================
    // UPDATE ATTENDANCE
    // ==========================================

    @PostMapping("/attendance/{id}")
    public String updateAttendance(
            @PathVariable Long id,
            @ModelAttribute Attendance attendance,
            Authentication authentication) {

        attendanceService.updateAttendance(
                id,
                attendance,
                authentication);

        return "redirect:/attendance";
    }

    // ==========================================
    // DELETE ATTENDANCE
    // ==========================================

    @PostMapping("/attendance/{id}/delete")
    public String deleteAttendance(
            @PathVariable Long id,
            Authentication authentication) {

        attendanceService.deleteAttendance(id, authentication);

        return "redirect:/attendance";
    }
}