package com.learnspherex.reporting.service;

import com.learnspherex.exception.ResourceNotFoundException;
import com.learnspherex.project.repository.*;
import com.learnspherex.student.entity.EnrollmentStatus;
import com.learnspherex.student.repository.*;
import com.learnspherex.course.repository.CourseRepository;
import com.learnspherex.batch.repository.BatchRepository;
import com.learnspherex.reporting.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
    private final StudentRepository studentRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final ProjectRepository projectRepository;
    private final ProjectSubmissionRepository submissionRepository;
    private final ProjectEvaluationRepository evaluationRepository;

    public ReportService(StudentRepository studentRepository,
                         StudentCourseRepository studentCourseRepository,
                         CourseRepository courseRepository,
                         BatchRepository batchRepository,
                         ProjectRepository projectRepository,
                         ProjectSubmissionRepository submissionRepository,
                         ProjectEvaluationRepository evaluationRepository) {
        this.studentRepository = studentRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.courseRepository = courseRepository;
        this.batchRepository = batchRepository;
        this.projectRepository = projectRepository;
        this.submissionRepository = submissionRepository;
        this.evaluationRepository = evaluationRepository;
    }

    @Transactional(readOnly = true)
    public StudentReportResponse student(Long id) {
        var s = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        long totalCourses = studentCourseRepository.countByStudentId(id);
        long activeCourses = studentCourseRepository.countByStudentIdAndStatus(id, EnrollmentStatus.ACTIVE);
        var evaluations = evaluationRepository.findAll().stream()
                .filter(e -> e.getSubmission().getStudent().getId().equals(id)).toList();

        Double avg = averagePercentage(evaluations);
        return new StudentReportResponse(id, s.getStudentCode(), totalCourses, activeCourses,
                submissionRepository.countByStudentId(id), evaluations.size(), avg, avg);
    }

    @Transactional(readOnly = true)
    public BatchReportResponse batch(Long id) {
        var batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + id));
        var enrollments = studentCourseRepository.findAll().stream()
                .filter(e -> e.getBatch().getId().equals(id)).toList();
        var studentIds = enrollments.stream().map(e -> e.getStudent().getId()).distinct().toList();
        Long courseId = batch.getCourseId();

        // Must also check the submission's project belongs to THIS batch's
        // course - a student enrolled in more than one batch/course otherwise
        // has unrelated submissions counted into this report.
        var submissions = submissionRepository.findAll().stream()
                .filter(s -> studentIds.contains(s.getStudent().getId())
                        && courseId != null
                        && courseId.equals(s.getProject().getCourse().getId()))
                .toList();
        var evaluations = submissions.stream()
                .map(s -> evaluationRepository.findBySubmissionId(s.getId()).orElse(null))
                .filter(java.util.Objects::nonNull).toList();

        return new BatchReportResponse(id, batch.getBatchCode(), studentIds.size(),
                enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                        .map(e -> e.getStudent().getId()).distinct().count(),
                submissions.size(), evaluations.size(), averagePercentage(evaluations));
    }

    @Transactional(readOnly = true)
    public CourseReportResponse course(Long id) {
        var course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));

        var enrollments = studentCourseRepository.findAll().stream()
                .filter(e -> e.getCourse().getId().equals(id)).toList();
        var studentIds = enrollments.stream().map(e -> e.getStudent().getId()).distinct().toList();
        var projects = projectRepository.findByCourseId(id);
        var submissions = submissionRepository.findAll().stream()
                .filter(s -> projects.stream().anyMatch(p -> p.getId().equals(s.getProject().getId())))
                .toList();
        var evaluations = submissions.stream()
                .map(s -> evaluationRepository.findBySubmissionId(s.getId()).orElse(null))
                .filter(java.util.Objects::nonNull).toList();

        return new CourseReportResponse(id, course.getCourseName(), studentIds.size(),
                enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                        .map(e -> e.getStudent().getId()).distinct().count(),
                projects.size(), submissions.size(), evaluations.size(), averagePercentage(evaluations));
    }

    @Transactional(readOnly = true)
    public ProjectReportResponse project(Long id) {
        var project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        var submissions = submissionRepository.findByProjectId(id);
        var evaluations = submissions.stream()
                .map(s -> evaluationRepository.findBySubmissionId(s.getId()).orElse(null))
                .filter(java.util.Objects::nonNull).toList();

        Double avgMarks = evaluations.isEmpty() ? null :
                Math.round(evaluations.stream().mapToInt(e -> e.getTotalMarks()).average().orElse(0) * 100.0) / 100.0;
        Double avgPercentage = averagePercentage(evaluations);
        Integer highest = evaluations.isEmpty() ? null : evaluations.stream().mapToInt(e -> e.getTotalMarks()).max().orElse(0);
        Integer lowest = evaluations.isEmpty() ? null : evaluations.stream().mapToInt(e -> e.getTotalMarks()).min().orElse(0);

        return new ProjectReportResponse(id, project.getTitle(), project.getMaximumMarks(),
                submissions.size(), evaluations.size(), submissions.size() - evaluations.size(),
                avgMarks, avgPercentage, highest, lowest);
    }

    private Double averagePercentage(java.util.List<com.learnspherex.project.entity.ProjectEvaluation> evaluations) {
        if (evaluations.isEmpty()) return null;
        double avg = evaluations.stream().mapToDouble(e -> {
            int max = e.getSubmission().getProject().getMaximumMarks();
            return max == 0 ? 0 : e.getTotalMarks() * 100.0 / max;
        }).average().orElse(0);
        return Math.round(avg * 100.0) / 100.0;
    }
}
