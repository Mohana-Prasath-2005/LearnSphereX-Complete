package com.learnspherex.batch.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.learnspherex.batch.entity.Attendance;
import com.learnspherex.batch.entity.AttendanceStatus;
import com.learnspherex.batch.entity.Batch;
import com.learnspherex.batch.entity.BatchSession;
import com.learnspherex.batch.repository.AttendanceRepository;
import com.learnspherex.batch.repository.BatchEnrollmentRepository;
import com.learnspherex.batch.repository.BatchRepository;
import com.learnspherex.batch.repository.BatchSessionRepository;
import com.learnspherex.exception.UnauthorizedOperationException;
import com.learnspherex.notification.event.NotificationEvent;
import com.learnspherex.security.CurrentUserService;
import com.learnspherex.student.entity.Student;
import com.learnspherex.student.repository.StudentRepository;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final double LOW_ATTENDANCE_LIMIT = 75.0;

    private final AttendanceRepository attendanceRepository;
    private final BatchRepository batchRepository;
    private final BatchEnrollmentRepository enrollmentRepository;
    private final BatchSessionRepository sessionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentUserService currentUserService;
    private final StudentRepository studentRepository;

    public AttendanceServiceImpl(
            AttendanceRepository attendanceRepository,
            BatchRepository batchRepository,
            BatchEnrollmentRepository enrollmentRepository,
            BatchSessionRepository sessionRepository,
            ApplicationEventPublisher eventPublisher,
            CurrentUserService currentUserService,
            StudentRepository studentRepository) {

        this.attendanceRepository = attendanceRepository;
        this.batchRepository = batchRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.sessionRepository = sessionRepository;
        this.eventPublisher = eventPublisher;
        this.currentUserService = currentUserService;
        this.studentRepository = studentRepository;
    }

    // ==========================================
    // AUTHORIZATION HELPERS
    // ==========================================

    /**
     * Only ADMIN or the trainer actually assigned to this batch may
     * mark/update/delete attendance or pull batch-scoped reports.
     */
    private void assertBatchStaff(Authentication authentication, Long batchId) {
        if (currentUserService.hasRole(authentication, "ADMIN")) {
            return;
        }
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Batch not found with id: " + batchId));
        currentUserService.assertOwnerOrRole(authentication, batch.getTrainerId());
    }

    /**
     * ADMIN, the batch's assigned trainer, or the student the record belongs to.
     * Attendance.studentId is the Student table's PK, not the login user id,
     * so it has to be resolved to Student.userId before comparing.
     */
    private void assertBatchStaffOrOwningStudent(Authentication authentication, Long batchId, Long studentId) {
        if (currentUserService.hasRole(authentication, "ADMIN")) {
            return;
        }
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Batch not found with id: " + batchId));
        if (batch.getTrainerId() != null
                && batch.getTrainerId().equals(currentUserService.currentUser(authentication).getId())) {
            return;
        }
        Long ownerUserId = studentRepository.findById(studentId).map(Student::getUserId).orElse(null);
        currentUserService.assertOwnerOrRole(authentication, ownerUserId);
    }

    /**
     * For endpoints keyed only by studentId (no batch in scope): the student
     * themselves, any TRAINER, or ADMIN.
     */
    private void assertStudentOwnerOrStaff(Authentication authentication, Long studentId) {
        if (currentUserService.hasRole(authentication, "TRAINER")) {
            return;
        }
        Long ownerUserId = studentRepository.findById(studentId).map(Student::getUserId).orElse(null);
        currentUserService.assertOwnerOrRole(authentication, ownerUserId, "ADMIN");
    }

    // ==========================================
    // MARK ATTENDANCE
    // ==========================================

    @Override
    public Attendance markAttendance(
            Attendance attendance,
            Authentication authentication) {

        validateAttendance(attendance);

        // Check whether batch exists
        validateBatch(attendance.getBatchId());

        // Only ADMIN or the trainer assigned to this batch may mark attendance
        assertBatchStaff(authentication, attendance.getBatchId());

        // The student must actually exist and be enrolled in this batch -
        // otherwise attendance % silently gets corrupted by phantom records.
        if (!studentRepository.existsById(attendance.getStudentId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Student not found with id: " + attendance.getStudentId());
        }

        if (!enrollmentRepository.existsByBatchIdAndStudentId(
                attendance.getBatchId(), attendance.getStudentId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Student " + attendance.getStudentId()
                            + " is not enrolled in batch " + attendance.getBatchId());
        }

        validateAndLinkSession(attendance);

        boolean alreadyMarked = attendance.getSessionId() != null
                ? attendanceRepository.existsByBatchIdAndStudentIdAndSessionId(
                        attendance.getBatchId(), attendance.getStudentId(), attendance.getSessionId())
                : attendanceRepository.existsByBatchIdAndStudentIdAndAttendanceDate(
                        attendance.getBatchId(), attendance.getStudentId(), attendance.getAttendanceDate());

        if (alreadyMarked) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Attendance already marked for this student "
                            + "on "
                            + attendance.getAttendanceDate()
            );
        }

        Attendance savedAttendance =
                attendanceRepository.save(attendance);

        // Check whether attendance has fallen below 75%
        checkLowAttendance(
                savedAttendance.getBatchId(),
                savedAttendance.getStudentId()
        );

        return savedAttendance;
    }

    // ==========================================
    // GET ALL ATTENDANCE
    // ==========================================

    @Override
    public List<Attendance> getAllAttendance() {

        return attendanceRepository.findAll();
    }

    // ==========================================
    // GET ATTENDANCE BY ID
    // ==========================================

    @Override
    public Attendance getAttendanceById(Long id, Authentication authentication) {

        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Attendance not found with id: "
                                        + id
                        ));

        assertBatchStaffOrOwningStudent(authentication, attendance.getBatchId(), attendance.getStudentId());

        return attendance;
    }

    // ==========================================
    // GET ATTENDANCE BY BATCH
    // ==========================================

    @Override
    public List<Attendance> getAttendanceByBatchId(
            Long batchId, Authentication authentication) {

        validateBatch(batchId);
        assertBatchStaff(authentication, batchId);

        return attendanceRepository
                .findByBatchId(batchId);
    }

    // ==========================================
    // GET ATTENDANCE BY STUDENT
    // ==========================================

    @Override
    public List<Attendance> getAttendanceByStudentId(
            Long studentId, Authentication authentication) {

        assertStudentOwnerOrStaff(authentication, studentId);

        return attendanceRepository
                .findByStudentId(studentId);
    }

    // ==========================================
    // GET ATTENDANCE BY BATCH + STUDENT
    // ==========================================

    @Override
    public List<Attendance> getAttendanceByBatchAndStudent(
            Long batchId,
            Long studentId,
            Authentication authentication) {

        validateBatch(batchId);
        assertBatchStaffOrOwningStudent(authentication, batchId, studentId);

        return attendanceRepository
                .findByBatchIdAndStudentId(
                        batchId,
                        studentId
                );
    }

    // ==========================================
    // UPDATE ATTENDANCE
    // ==========================================

    @Override
    public Attendance updateAttendance(
            Long id,
            Attendance attendance,
            Authentication authentication) {

        // Make sure attendance record exists
        Attendance existing =
                attendanceRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Attendance not found with id: " + id));

        validateAttendance(attendance);

        // Make sure batch exists
        validateBatch(attendance.getBatchId());

        // Only ADMIN or the trainer of the (possibly new) batch may update
        assertBatchStaff(authentication, existing.getBatchId());
        assertBatchStaff(authentication, attendance.getBatchId());

        validateAndLinkSession(attendance);

        /*
         * Check whether ANOTHER attendance record already has the same
         * (batchId, studentId, sessionId) - or (batchId, studentId,
         * attendanceDate) when no session is linked. The current record ID
         * is excluded.
         */
        boolean duplicate = attendance.getSessionId() != null
                ? attendanceRepository.existsByBatchIdAndStudentIdAndSessionIdAndIdNot(
                        attendance.getBatchId(), attendance.getStudentId(), attendance.getSessionId(), id)
                : attendanceRepository.existsByBatchIdAndStudentIdAndAttendanceDateAndIdNot(
                        attendance.getBatchId(), attendance.getStudentId(), attendance.getAttendanceDate(), id);

        if (duplicate) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Attendance already exists for this "
                            + "student on this date"
            );
        }

        existing.setBatchId(
                attendance.getBatchId());

        existing.setStudentId(
                attendance.getStudentId());

        existing.setAttendanceDate(
                attendance.getAttendanceDate());

        existing.setSessionId(
                attendance.getSessionId());

        existing.setStatus(
                attendance.getStatus());

        Attendance updatedAttendance =
                attendanceRepository.save(existing);

        // Check low attendance after update
        checkLowAttendance(
                updatedAttendance.getBatchId(),
                updatedAttendance.getStudentId()
        );

        return updatedAttendance;
    }

    // ==========================================
    // DELETE ATTENDANCE
    // ==========================================

    @Override
    public void deleteAttendance(Long id, Authentication authentication) {

        Attendance existing =
                attendanceRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Attendance not found with id: " + id));

        assertBatchStaff(authentication, existing.getBatchId());

        attendanceRepository.delete(existing);
    }

    // ==========================================
    // CALCULATE ATTENDANCE PERCENTAGE
    // ==========================================

    @Override
    public double calculateAttendancePercentage(
            Long batchId,
            Long studentId,
            Authentication authentication) {

        validateBatch(batchId);
        assertBatchStaffOrOwningStudent(authentication, batchId, studentId);

        return computePercentage(batchId, studentId);
    }

    /**
     * Internal calculator with no authorization check, used by system-initiated
     * paths (low-attendance sweep) that already established the caller's
     * permission to view the batch, or run outside a specific user's request.
     */
    private double computePercentage(
            Long batchId,
            Long studentId) {

        List<Attendance> records =
                attendanceRepository
                        .findByBatchIdAndStudentId(
                                batchId,
                                studentId
                        );

        if (records.isEmpty()) {
            return 0.0;
        }

        /*
         * Spec formula: Present Sessions / Total Sessions x 100. "Total
         * Sessions" means sessions actually held so far, not "however many
         * attendance rows happen to have been marked" - a trainer skipping
         * marking for some held sessions must not silently inflate the %.
         * Fall back to records.size() only if this batch has no BatchSession
         * data yet (e.g. attendance predates the schedule->session feature).
         */
        long totalSessionsHeld = sessionRepository.findByBatchId(batchId).stream()
                .filter(s -> !s.getSessionDate().isAfter(LocalDate.now()))
                .count();

        long denominator = totalSessionsHeld > 0 ? totalSessionsHeld : records.size();

        /*
         * Only PRESENT counts, per the spec's literal "Present Sessions"
         * wording - LATE no longer counts the same as PRESENT.
         */
        long attendedSessions =
                records.stream()
                        .filter(attendance ->
                                attendance.getStatus()
                                        == AttendanceStatus.PRESENT
                        )
                        .count();

        return ((double) attendedSessions
                / denominator) * 100.0;
    }

    // ==========================================
    // GET MONTHLY ATTENDANCE
    // ==========================================

    @Override
    public List<Attendance> getMonthlyAttendance(
            Long studentId,
            int year,
            int month,
            Authentication authentication) {

        assertStudentOwnerOrStaff(authentication, studentId);

        if (month < 1 || month > 12) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Month must be between 1 and 12"
            );
        }

        YearMonth yearMonth =
                YearMonth.of(year, month);

        LocalDate startDate =
                yearMonth.atDay(1);

        LocalDate endDate =
                yearMonth.atEndOfMonth();

        return attendanceRepository
                .findByStudentIdAndAttendanceDateBetween(
                        studentId,
                        startDate,
                        endDate
                );
    }

    // ==========================================
    // BATCH ATTENDANCE REPORT
    // ==========================================

    @Override
    public List<Attendance> getBatchAttendanceReport(
            Long batchId,
            LocalDate startDate,
            LocalDate endDate,
            Authentication authentication) {

        validateBatch(batchId);
        assertBatchStaff(authentication, batchId);

        if (startDate == null
                || endDate == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start date and end date are required"
            );
        }

        if (endDate.isBefore(startDate)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date cannot be before start date"
            );
        }

        return attendanceRepository
                .findByBatchIdAndAttendanceDateBetween(
                        batchId,
                        startDate,
                        endDate
                );
    }

    // ==========================================
    // LOW ATTENDANCE STUDENTS
    // ==========================================

    @Override
    public Map<Long, Double> getLowAttendanceStudents(
            Long batchId, Authentication authentication) {

        validateBatch(batchId);
        assertBatchStaff(authentication, batchId);

        List<Attendance> records =
                attendanceRepository
                        .findByBatchId(batchId);

        Map<Long, Double> result =
                new HashMap<>();

        records.stream()
                .map(Attendance::getStudentId)
                .distinct()
                .forEach(studentId -> {

                    double percentage =
                            computePercentage(
                                    batchId,
                                    studentId
                            );

                    if (percentage
                            < LOW_ATTENDANCE_LIMIT) {

                        result.put(
                                studentId,
                                percentage
                        );
                    }
                });

        return result;
    }

    // ==========================================
    // LOW ATTENDANCE CHECK
    // ==========================================

    private void checkLowAttendance(
            Long batchId,
            Long studentId) {

        double percentage =
                computePercentage(
                        batchId,
                        studentId
                );

        if (percentage < LOW_ATTENDANCE_LIMIT) {

            // Notification.userId must be the login User.id, not Student.id
            // (Attendance.studentId) - these are different values.
            Long ownerUserId = studentRepository.findById(studentId)
                    .map(Student::getUserId)
                    .orElse(null);

            eventPublisher.publishEvent(
                    new NotificationEvent(
                            ownerUserId,
                            null,
                            "Low Attendance Alert",
                            "Your attendance is "
                                    + String.format(
                                            "%.2f",
                                            percentage
                                    )
                                    + "%. Please maintain "
                                    + "at least 75% attendance.",
                            "LOW_ATTENDANCE"
                    )
            );
        }
    }

    // ==========================================
    // VALIDATE ATTENDANCE
    // ==========================================

    private void validateAttendance(
            Attendance attendance) {

        if (attendance == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Attendance data is required"
            );
        }

        if (attendance.getBatchId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Batch ID is required"
            );
        }

        if (attendance.getStudentId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Student ID is required"
            );
        }

        if (attendance.getAttendanceDate() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Attendance date is required"
            );
        }

        if (attendance.getStatus() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Attendance status is required"
            );
        }
    }

    // ==========================================
    // VALIDATE + LINK SESSION (optional)
    // ==========================================

    /**
     * If a sessionId was supplied, verify it exists and belongs to the same
     * batch, and derive attendanceDate from it (the session is authoritative,
     * not whatever date the caller happened to send).
     */
    private void validateAndLinkSession(Attendance attendance) {

        if (attendance.getSessionId() == null) {
            return;
        }

        BatchSession session = sessionRepository.findById(attendance.getSessionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found with id: " + attendance.getSessionId()));

        if (!session.getBatchId().equals(attendance.getBatchId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Session " + attendance.getSessionId() + " does not belong to batch "
                            + attendance.getBatchId());
        }

        attendance.setAttendanceDate(session.getSessionDate());
    }

    // ==========================================
    // VALIDATE BATCH
    // ==========================================

    private void validateBatch(Long batchId) {

        if (batchId == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Batch ID is required"
            );
        }

        batchRepository.findById(batchId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Batch not found with id: "
                                        + batchId
                        ));
    }
}