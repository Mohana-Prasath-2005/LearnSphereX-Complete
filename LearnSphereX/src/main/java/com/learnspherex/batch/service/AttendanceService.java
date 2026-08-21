package com.learnspherex.batch.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;

import com.learnspherex.batch.entity.Attendance;

public interface AttendanceService {

    // ==========================================
    // CREATE
    // ==========================================

    Attendance markAttendance(Attendance attendance, Authentication authentication);

    // ==========================================
    // GET ALL
    // ==========================================

    List<Attendance> getAllAttendance();

    // ==========================================
    // GET BY ID
    // ==========================================

    Attendance getAttendanceById(Long id, Authentication authentication);

    // ==========================================
    // GET BY BATCH
    // ==========================================

    List<Attendance> getAttendanceByBatchId(
            Long batchId, Authentication authentication);

    // ==========================================
    // GET BY STUDENT
    // ==========================================

    List<Attendance> getAttendanceByStudentId(
            Long studentId, Authentication authentication);

    // ==========================================
    // GET BY STUDENT + BATCH
    // ==========================================

    List<Attendance> getAttendanceByBatchAndStudent(
            Long batchId,
            Long studentId,
            Authentication authentication);

    // ==========================================
    // UPDATE
    // ==========================================

    Attendance updateAttendance(
            Long id,
            Attendance attendance,
            Authentication authentication);

    // ==========================================
    // DELETE
    // ==========================================

    void deleteAttendance(Long id, Authentication authentication);

    // ==========================================
    // ATTENDANCE PERCENTAGE
    // ==========================================

    double calculateAttendancePercentage(
            Long batchId,
            Long studentId,
            Authentication authentication);

    // ==========================================
    // MONTHLY ATTENDANCE
    // ==========================================

    List<Attendance> getMonthlyAttendance(
            Long studentId,
            int year,
            int month,
            Authentication authentication);

    // ==========================================
    // BATCH ATTENDANCE REPORT
    // ==========================================

    List<Attendance> getBatchAttendanceReport(
            Long batchId,
            LocalDate startDate,
            LocalDate endDate,
            Authentication authentication);

    // ==========================================
    // LOW ATTENDANCE
    // ==========================================

    Map<Long, Double> getLowAttendanceStudents(
            Long batchId, Authentication authentication);
}