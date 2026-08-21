package com.learnspherex.project.service;

import com.learnspherex.exception.BadRequestException;
import com.learnspherex.exception.DuplicateResourceException;
import com.learnspherex.exception.ResourceNotFoundException;
import com.learnspherex.project.dto.*;
import com.learnspherex.project.entity.*;
import com.learnspherex.project.repository.*;
import com.learnspherex.security.CurrentUserService;
import com.learnspherex.student.entity.Student;
import com.learnspherex.student.repository.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ProjectSubmissionService {
    private final ProjectSubmissionRepository repository;
    private final ProjectRepository projectRepository;
    private final StudentRepository studentRepository;
    private final CurrentUserService currentUserService;

    public ProjectSubmissionService(ProjectSubmissionRepository repository,
                                    ProjectRepository projectRepository,
                                    StudentRepository studentRepository,
                                    CurrentUserService currentUserService) {
        this.repository = repository;
        this.projectRepository = projectRepository;
        this.studentRepository = studentRepository;
        this.currentUserService = currentUserService;
    }

    private void assertSubmissionAccess(Authentication authentication, ProjectSubmission submission) {
        currentUserService.assertAnyOwnerOrRole(authentication,
                List.of(submission.getStudent().getUserId(), submission.getProject().getTrainerId()),
                "ADMIN");
    }

    public SubmissionResponse submit(Long projectId, SubmissionRequest r, Authentication authentication) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        Student student = studentRepository.findById(r.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + r.studentId()));
        currentUserService.assertOwnerOrRole(authentication, student.getUserId(), "ADMIN");

        if (repository.findByProjectIdAndStudentId(projectId, r.studentId()).isPresent())
            throw new DuplicateResourceException("Student already has a submission for this project");

        SubmissionStatus status = LocalDate.now().isAfter(project.getDeadline())
                ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED;

        ProjectSubmission submission = ProjectSubmission.builder()
                .project(project).student(student).githubUrl(r.githubUrl())
                .deploymentUrl(r.deploymentUrl()).description(r.description())
                .submittedAt(LocalDateTime.now()).version(r.version()).status(status).build();

        return toResponse(repository.save(submission));
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> findAll(Authentication authentication) {
        currentUserService.assertOwnerOrRole(authentication, null, "ADMIN", "TRAINER", "EVALUATOR");
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> findByStudent(Long studentId, Authentication authentication) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        currentUserService.assertOwnerOrRole(authentication, student.getUserId(), "ADMIN", "TRAINER");
        return repository.findByStudentId(studentId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> findByProject(Long projectId, Authentication authentication) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        currentUserService.assertOwnerOrRole(authentication, project.getTrainerId(), "ADMIN");
        return repository.findByProjectId(projectId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubmissionResponse findById(Long id, Authentication authentication) {
        ProjectSubmission submission = getEntity(id);
        assertSubmissionAccess(authentication, submission);
        return toResponse(submission);
    }

    public ProjectSubmission getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + id));
    }

    public void markEvaluated(ProjectSubmission submission) {
        submission.setStatus(SubmissionStatus.EVALUATED);
        repository.save(submission);
    }

    private SubmissionResponse toResponse(ProjectSubmission s) {
        return new SubmissionResponse(s.getId(), s.getProject().getId(), s.getProject().getTitle(),
                s.getStudent().getId(), s.getStudent().getStudentCode(), s.getGithubUrl(),
                s.getDeploymentUrl(), s.getDescription(), s.getSubmittedAt(),
                s.getVersion(), s.getStatus());
    }
}
