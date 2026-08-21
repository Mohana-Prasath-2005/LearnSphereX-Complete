package com.learnspherex.student.service;

import com.learnspherex.assignment.entity.Assignment;
import com.learnspherex.assignment.repository.AssignmentRepository;
import com.learnspherex.assignment.repository.AssignmentSubmissionRepository;
import com.learnspherex.auth.User;
import com.learnspherex.auth.UserRepository;
import com.learnspherex.batch.repository.AttendanceRepository;
import com.learnspherex.batch.entity.AttendanceStatus;
import com.learnspherex.certificate.repository.CertificateRepository;
import com.learnspherex.examination.entity.ExamAttempt;
import com.learnspherex.examination.repository.ExamAttemptRepository;
import com.learnspherex.project.entity.ProjectEvaluation;
import com.learnspherex.project.repository.ProjectEvaluationRepository;
import com.learnspherex.project.repository.ProjectSubmissionRepository;
import com.learnspherex.security.CurrentUserService;
import com.learnspherex.student.dto.StudentDashboardResponse;
import com.learnspherex.student.entity.EnrollmentStatus;
import com.learnspherex.student.entity.StudentCourse;
import com.learnspherex.student.repository.StudentCourseRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentDashboardService {
    private final StudentService studentService;
    private final StudentCourseRepository studentCourseRepository;
    private final ProjectSubmissionRepository submissionRepository;
    private final ProjectEvaluationRepository evaluationRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final CertificateRepository certificateRepository;

    public StudentDashboardService(StudentService studentService,
                                   StudentCourseRepository studentCourseRepository,
                                   ProjectSubmissionRepository submissionRepository,
                                   ProjectEvaluationRepository evaluationRepository,
                                   CurrentUserService currentUserService,
                                   UserRepository userRepository,
                                   AttendanceRepository attendanceRepository,
                                   AssignmentRepository assignmentRepository,
                                   AssignmentSubmissionRepository assignmentSubmissionRepository,
                                   ExamAttemptRepository examAttemptRepository,
                                   CertificateRepository certificateRepository) {
        this.studentService = studentService;
        this.studentCourseRepository = studentCourseRepository;
        this.submissionRepository = submissionRepository;
        this.evaluationRepository = evaluationRepository;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.certificateRepository = certificateRepository;
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse getDashboard(Long studentId, Authentication authentication) {
        var student = studentService.getEntity(studentId);
        currentUserService.assertOwnerOrRole(authentication, student.getUserId(), "ADMIN", "TRAINER");

        User user = userRepository.findById(student.getUserId()).orElse(null);
        String studentName = user != null
                ? (user.getFirstName() + " " + user.getLastName())
                : student.getStudentCode();

        List<StudentCourse> enrollments = studentCourseRepository.findByStudentId(studentId);
        long totalCourses = enrollments.size();
        long activeCourses = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .count();
        long activeBatches = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .map(e -> e.getBatch().getId())
                .distinct()
                .count();

        double attendancePercentage = computeAttendancePercentage(studentId);

        Set<Long> courseIds = enrollments.stream()
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toSet());
        List<Assignment> assignments = courseIds.stream()
                .flatMap(courseId -> assignmentRepository.findByCourseId(courseId).stream())
                .toList();
        Set<Long> submittedAssignmentIds = assignmentSubmissionRepository.findByStudentId(studentId).stream()
                .map(s -> s.getAssignment().getId())
                .collect(Collectors.toSet());
        long totalAssignments = assignments.size();
        long completedAssignments = assignments.stream()
                .filter(a -> submittedAssignmentIds.contains(a.getId()))
                .count();
        long pendingAssignments = totalAssignments - completedAssignments;

        List<ExamAttempt> attempts = examAttemptRepository.findByStudentId(studentId).stream()
                .filter(ExamAttempt::isSubmitted)
                .toList();
        long totalTests = attempts.size();
        Double averageTestScore = attempts.isEmpty() ? null
                : round(attempts.stream()
                        .mapToDouble(a -> a.getPercentage().doubleValue())
                        .average().orElse(0));

        long totalProjects = submissionRepository.countByStudentId(studentId);
        long evaluated = evaluationRepository.countBySubmissionStudentId(studentId);

        Double projectAverage = null;
        var evaluations = evaluationRepository.findAll().stream()
                .filter(e -> e.getSubmission().getStudent().getId().equals(studentId))
                .toList();
        if (!evaluations.isEmpty()) {
            projectAverage = round(evaluations.stream()
                    .mapToDouble(this::percentage)
                    .average().orElse(0));
        }

        long totalCertificates = certificateRepository.findByStudentId(studentId).size();

        return new StudentDashboardResponse(studentId, studentName, student.getStudentCode(),
                totalCourses, activeCourses, activeBatches, attendancePercentage,
                totalAssignments, completedAssignments, pendingAssignments,
                totalTests, averageTestScore,
                totalProjects, evaluated, projectAverage,
                totalCertificates);
    }

    private double computeAttendancePercentage(Long studentId) {
        var records = attendanceRepository.findByStudentId(studentId);
        if (records.isEmpty()) {
            return 0.0;
        }
        long attended = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE)
                .count();
        return round(((double) attended / records.size()) * 100.0);
    }

    private double percentage(ProjectEvaluation e) {
        int max = e.getSubmission().getProject().getMaximumMarks();
        return max == 0 ? 0 : e.getTotalMarks() * 100.0 / max;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
