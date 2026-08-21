package com.learnspherex.batch.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnspherex.batch.entity.Attendance;

public interface AttendanceRepository
        extends JpaRepository<Attendance, Long> {

    // ==========================================
    // BATCH ATTENDANCE
    // ==========================================

    List<Attendance> findByBatchId(Long batchId);

    // ==========================================
    // STUDENT ATTENDANCE
    // ==========================================

    List<Attendance> findByStudentId(Long studentId);

    // ==========================================
    // STUDENT + BATCH ATTENDANCE
    // ==========================================

    List<Attendance> findByBatchIdAndStudentId(
            Long batchId,
            Long studentId);

    // ==========================================
    // STUDENT MONTHLY ATTENDANCE
    // ==========================================

    List<Attendance> findByStudentIdAndAttendanceDateBetween(
            Long studentId,
            LocalDate startDate,
            LocalDate endDate);

    // ==========================================
    // BATCH ATTENDANCE REPORT
    // ==========================================

    List<Attendance> findByBatchIdAndAttendanceDateBetween(
            Long batchId,
            LocalDate startDate,
            LocalDate endDate);

    // ==========================================
    // CREATE DUPLICATE CHECK
    // ==========================================

    boolean existsByBatchIdAndStudentIdAndAttendanceDate(
            Long batchId,
            Long studentId,
            LocalDate attendanceDate);

    // ==========================================
    // UPDATE DUPLICATE CHECK
    // ==========================================

    boolean existsByBatchIdAndStudentIdAndAttendanceDateAndIdNot(
            Long batchId,
            Long studentId,
            LocalDate attendanceDate,
            Long id);

    // ==========================================
    // SESSION-SCOPED DUPLICATE CHECKS
    // (disambiguates two sessions on the same day)
    // ==========================================

    boolean existsByBatchIdAndStudentIdAndSessionId(
            Long batchId,
            Long studentId,
            Long sessionId);

    boolean existsByBatchIdAndStudentIdAndSessionIdAndIdNot(
            Long batchId,
            Long studentId,
            Long sessionId,
            Long id);
}