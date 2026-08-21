package com.learnspherex.certificate.service;

import com.learnspherex.auth.User;
import com.learnspherex.auth.UserRepository;
import com.learnspherex.batch.entity.Attendance;
import com.learnspherex.batch.entity.AttendanceStatus;
import com.learnspherex.batch.entity.Batch;
import com.learnspherex.batch.repository.AttendanceRepository;
import com.learnspherex.certificate.repository.CertificateRepository;
import com.learnspherex.course.entity.Course;
import com.learnspherex.course.repository.CourseRepository;
import com.learnspherex.examination.entity.Exam;
import com.learnspherex.examination.entity.ExamAttempt;
import com.learnspherex.examination.entity.ExamAttemptStatus;
import com.learnspherex.examination.repository.ExamAttemptRepository;
import com.learnspherex.exception.InvalidOperationException;
import com.learnspherex.project.entity.Project;
import com.learnspherex.project.entity.ProjectEvaluation;
import com.learnspherex.project.entity.ProjectSubmission;
import com.learnspherex.project.repository.ProjectEvaluationRepository;
import com.learnspherex.project.repository.ProjectSubmissionRepository;
import com.learnspherex.student.entity.EnrollmentStatus;
import com.learnspherex.student.entity.Student;
import com.learnspherex.student.entity.StudentCourse;
import com.learnspherex.student.repository.StudentCourseRepository;
import com.learnspherex.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock private CertificateRepository certs;
    @Mock private StudentRepository students;
    @Mock private StudentCourseRepository enrollments;
    @Mock private CourseRepository courses;
    @Mock private AttendanceRepository attendance;
    @Mock private ProjectSubmissionRepository submissions;
    @Mock private ProjectEvaluationRepository evaluations;
    @Mock private ExamAttemptRepository attempts;
    @Mock private UserRepository users;
    @Mock private ApplicationEventPublisher eventPublisher;

    private CertificateService service;

    private static final Long STUDENT_ID = 1L;
    private static final Long COURSE_ID = 2L;
    private static final Long BATCH_ID = 3L;

    private Student student;
    private Course course;
    private Batch batch;
    private StudentCourse enrollment;

    @BeforeEach
    void setUp() {
        service = new CertificateService(certs, students, enrollments, courses, attendance, submissions,
                evaluations, attempts, users, eventPublisher);

        student = Student.builder().id(STUDENT_ID).userId(100L).studentCode("STU-001").build();
        course = Course.builder().id(COURSE_ID).courseCode("JFS").courseName("Java Full Stack").build();
        batch = new Batch();
        batch.setId(BATCH_ID);
        enrollment = StudentCourse.builder().student(student).course(course).batch(batch)
                .status(EnrollmentStatus.COMPLETED).build();

        when(students.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(enrollments.findFirstByStudentIdAndCourseId(STUDENT_ID, COURSE_ID)).thenReturn(Optional.of(enrollment));
    }

    private Attendance attendanceRecord(AttendanceStatus status) {
        Attendance a = new Attendance();
        a.setStatus(status);
        return a;
    }

    @Test
    void belowSeventyFivePercentAttendanceBlocksCertificate() {
        when(attendance.findByBatchIdAndStudentId(BATCH_ID, STUDENT_ID)).thenReturn(List.of(
                attendanceRecord(AttendanceStatus.PRESENT),
                attendanceRecord(AttendanceStatus.ABSENT),
                attendanceRecord(AttendanceStatus.ABSENT),
                attendanceRecord(AttendanceStatus.ABSENT)
        ));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> service.generate(STUDENT_ID, COURSE_ID));
        assertTrue(ex.getMessage().contains("Attendance"));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void projectEvaluatedButBelowPassingScoreBlocksCertificate() {
        when(attendance.findByBatchIdAndStudentId(BATCH_ID, STUDENT_ID)).thenReturn(List.of(
                attendanceRecord(AttendanceStatus.PRESENT),
                attendanceRecord(AttendanceStatus.PRESENT),
                attendanceRecord(AttendanceStatus.PRESENT),
                attendanceRecord(AttendanceStatus.ABSENT)
        ));

        Project project = Project.builder().id(5L).course(course).maximumMarks(100).build();
        ProjectSubmission submission = ProjectSubmission.builder().id(6L).project(project).student(student).build();
        when(submissions.findByStudentId(STUDENT_ID)).thenReturn(List.of(submission));

        // 2/100 = a project evaluation exists, but nowhere near the 50% pass bar.
        ProjectEvaluation evaluation = ProjectEvaluation.builder().id(7L).submission(submission).totalMarks(2).build();
        when(evaluations.findBySubmissionId(6L)).thenReturn(Optional.of(evaluation));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> service.generate(STUDENT_ID, COURSE_ID));
        assertTrue(ex.getMessage().contains("passing score"));
    }

    @Test
    void allFourConditionsMetIssuesCertificateWithComputedGrade() {
        when(attendance.findByBatchIdAndStudentId(BATCH_ID, STUDENT_ID)).thenReturn(List.of(
                attendanceRecord(AttendanceStatus.PRESENT),
                attendanceRecord(AttendanceStatus.PRESENT),
                attendanceRecord(AttendanceStatus.PRESENT),
                attendanceRecord(AttendanceStatus.PRESENT)
        ));

        Project project = Project.builder().id(5L).course(course).maximumMarks(100).build();
        ProjectSubmission submission = ProjectSubmission.builder().id(6L).project(project).student(student).build();
        when(submissions.findByStudentId(STUDENT_ID)).thenReturn(List.of(submission));

        ProjectEvaluation evaluation = ProjectEvaluation.builder().id(7L).submission(submission).totalMarks(85).build();
        when(evaluations.findBySubmissionId(6L)).thenReturn(Optional.of(evaluation));

        Exam exam = new Exam();
        exam.setId(8L);
        exam.setCourseId(COURSE_ID);
        exam.setPassingMarks(BigDecimal.valueOf(40));
        ExamAttempt attempt = new ExamAttempt(exam, STUDENT_ID);
        attempt.setSubmitted(true);
        attempt.setScore(BigDecimal.valueOf(80));
        attempt.setStatus(ExamAttemptStatus.PASSED);
        when(attempts.findAll()).thenReturn(List.of(attempt));

        when(certs.existsByStudentIdAndCourseId(STUDENT_ID, COURSE_ID)).thenReturn(false);
        when(courses.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(users.findById(100L)).thenReturn(Optional.of(
                new User("john", "john@x.com", "pw", "John", "Smith", null)));
        when(certs.countByCourseIdAndIssuedDateBetween(eq(COURSE_ID), any(), any())).thenReturn(0L);
        when(certs.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var certificate = service.generate(STUDENT_ID, COURSE_ID);

        assertEquals("A", certificate.getGrade());
        assertEquals("John Smith", certificate.getStudentName());
        assertTrue(certificate.getCertificateId().startsWith("CERT-JFS-"));
        verify(eventPublisher).publishEvent(isA(com.learnspherex.notification.event.NotificationEvent.class));
    }
}
