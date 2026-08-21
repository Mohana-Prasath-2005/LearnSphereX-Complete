package com.learnspherex.batch.service;

import com.learnspherex.batch.entity.*;
import com.learnspherex.batch.repository.*;
import com.learnspherex.security.CurrentUserService;
import com.learnspherex.student.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private BatchRepository batchRepository;
    @Mock private BatchEnrollmentRepository enrollmentRepository;
    @Mock private BatchSessionRepository sessionRepository;
    @Mock private CurrentUserService currentUserService;
    @Mock private StudentRepository studentRepository;
    @Mock private Authentication authentication;

    private AttendanceServiceImpl service() {
        return new AttendanceServiceImpl(attendanceRepository, batchRepository, enrollmentRepository,
                sessionRepository, null, currentUserService, studentRepository);
    }

    private Attendance record(Long batchId, Long studentId, AttendanceStatus status) {
        Attendance a = new Attendance();
        a.setBatchId(batchId);
        a.setStudentId(studentId);
        a.setStatus(status);
        a.setAttendanceDate(LocalDate.now().minusDays(1));
        return a;
    }

    private BatchSession heldSession(Long batchId, LocalDate date) {
        BatchSession s = new BatchSession();
        s.setBatchId(batchId);
        s.setSessionDate(date);
        return s;
    }

    // The core spec formula: Present Sessions / Total Sessions Held x 100 - not
    // "however many attendance rows exist," which is what the pre-fix bug used.
    @Test
    void percentageIsPresentDividedBySessionsHeldNotRecordCount() {
        when(currentUserService.hasRole(authentication, "ADMIN")).thenReturn(true);
        when(batchRepository.findById(10L)).thenReturn(Optional.of(new Batch()));
        when(attendanceRepository.findByBatchIdAndStudentId(10L, 1L)).thenReturn(List.of(
                record(10L, 1L, AttendanceStatus.PRESENT)
        ));
        // 4 sessions have actually been held so far, even though only 1 attendance row was ever marked.
        when(sessionRepository.findByBatchId(10L)).thenReturn(List.of(
                heldSession(10L, LocalDate.now().minusDays(3)),
                heldSession(10L, LocalDate.now().minusDays(2)),
                heldSession(10L, LocalDate.now().minusDays(1)),
                heldSession(10L, LocalDate.now())
        ));

        double pct = service().calculateAttendancePercentage(10L, 1L, authentication);

        assertEquals(25.0, pct, 0.001);
    }

    // LATE must not count as attended - only PRESENT does, per the spec's literal wording.
    @Test
    void lateDoesNotCountAsPresent() {
        when(currentUserService.hasRole(authentication, "ADMIN")).thenReturn(true);
        when(batchRepository.findById(20L)).thenReturn(Optional.of(new Batch()));
        when(attendanceRepository.findByBatchIdAndStudentId(20L, 2L)).thenReturn(List.of(
                record(20L, 2L, AttendanceStatus.PRESENT),
                record(20L, 2L, AttendanceStatus.LATE)
        ));
        when(sessionRepository.findByBatchId(20L)).thenReturn(List.of());

        double pct = service().calculateAttendancePercentage(20L, 2L, authentication);

        // No BatchSession rows -> falls back to records.size() (2) as the denominator; only 1 of 2 is PRESENT.
        assertEquals(50.0, pct, 0.001);
    }

    @Test
    void noAttendanceRecordsYieldsZeroPercent() {
        when(currentUserService.hasRole(authentication, "ADMIN")).thenReturn(true);
        when(batchRepository.findById(30L)).thenReturn(Optional.of(new Batch()));
        when(attendanceRepository.findByBatchIdAndStudentId(30L, 3L)).thenReturn(List.of());

        double pct = service().calculateAttendancePercentage(30L, 3L, authentication);

        assertEquals(0.0, pct, 0.001);
    }
}
