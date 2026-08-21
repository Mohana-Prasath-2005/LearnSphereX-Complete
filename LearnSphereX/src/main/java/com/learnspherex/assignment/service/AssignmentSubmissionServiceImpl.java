package com.learnspherex.assignment.service;

import com.learnspherex.assignment.entity.Assignment;
import com.learnspherex.assignment.entity.AssignmentEvaluation;
import com.learnspherex.assignment.entity.AssignmentSubmission;
import com.learnspherex.assignment.repository.AssignmentEvaluationRepository;
import com.learnspherex.assignment.repository.AssignmentRepository;
import com.learnspherex.assignment.repository.AssignmentSubmissionRepository;
import com.learnspherex.exception.InvalidOperationException;
import com.learnspherex.exception.ResourceNotFoundException;
import com.learnspherex.security.CurrentUserService;
import com.learnspherex.student.entity.Student;
import com.learnspherex.student.repository.StudentRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AssignmentSubmissionServiceImpl
        implements AssignmentSubmissionService {

    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentEvaluationRepository evaluationRepository;
    private final CurrentUserService currentUserService;
    private final StudentRepository studentRepository;

    public AssignmentSubmissionServiceImpl(
            AssignmentSubmissionRepository submissionRepository,
            AssignmentRepository assignmentRepository,
            AssignmentEvaluationRepository evaluationRepository,
            CurrentUserService currentUserService,
            StudentRepository studentRepository) {

        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.evaluationRepository = evaluationRepository;
        this.currentUserService = currentUserService;
        this.studentRepository = studentRepository;
    }

    /**
     * AssignmentSubmission.studentId is the Student table's PK, not the login
     * user id, so ownership has to be resolved through Student.userId.
     */
    private void assertOwnerOrStaff(Authentication authentication, Long studentId) {
        Long ownerUserId = studentRepository.findById(studentId).map(Student::getUserId).orElse(null);
        currentUserService.assertOwnerOrRole(authentication, ownerUserId, "ADMIN", "TRAINER");
    }

    @Override
    public AssignmentSubmission createSubmission(
            AssignmentSubmission submission,
            Authentication authentication) {

        if (submission.getAssignment() == null ||
                submission.getAssignment().getId() == null) {

            throw new InvalidOperationException(
                    "Assignment is required"
            );
        }

        if (submission.getStudentId() == null) {

            throw new InvalidOperationException(
                    "Student ID is required"
            );
        }

        Long ownerUserId = studentRepository.findById(submission.getStudentId())
                .map(Student::getUserId).orElse(null);
        currentUserService.assertOwnerOrRole(authentication, ownerUserId, "ADMIN");

        Long assignmentId =
                submission.getAssignment().getId();

        Assignment assignment =
                assignmentRepository.findById(assignmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Assignment not found with id: "
                                                + assignmentId
                                )
                        );

        List<AssignmentSubmission> previousSubmissions =
                submissionRepository
                        .findByAssignmentIdAndStudentIdOrderByVersionDesc(
                                assignmentId,
                                submission.getStudentId()
                        );

        int nextVersion = previousSubmissions.isEmpty()
                ? 1
                : previousSubmissions.get(0).getVersion() + 1;

        LocalDateTime now = LocalDateTime.now();

        submission.setAssignment(assignment);
        submission.setVersion(nextVersion);
        submission.setStatus(
                now.isAfter(assignment.getDeadline()) ? "LATE" : "SUBMITTED"
        );
        submission.setSubmittedAt(now);

        return submissionRepository.save(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentSubmission>
    getSubmissionsByAssignmentId(Long assignmentId) {

        if (!assignmentRepository.existsById(assignmentId)) {

            throw new ResourceNotFoundException(
                    "Assignment not found with id: " + assignmentId
            );
        }

        return submissionRepository
                .findByAssignmentIdOrderByVersionDesc(
                        assignmentId
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentSubmission>
    getSubmissionsByStudentId(Long studentId, Authentication authentication) {

        assertOwnerOrStaff(authentication, studentId);

        return submissionRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentSubmission>
    getStudentSubmissionsForAssignment(
            Long assignmentId,
            Long studentId,
            Authentication authentication) {

        if (!assignmentRepository.existsById(assignmentId)) {

            throw new ResourceNotFoundException(
                    "Assignment not found with id: " + assignmentId
            );
        }

        assertOwnerOrStaff(authentication, studentId);

        return submissionRepository
                .findByAssignmentIdAndStudentIdOrderByVersionDesc(
                        assignmentId,
                        studentId
                );
    }

    private AssignmentSubmission findEntity(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Submission not found with id: " + id
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentSubmission getSubmissionById(Long id, Authentication authentication) {

        AssignmentSubmission submission = findEntity(id);
        assertOwnerOrStaff(authentication, submission.getStudentId());
        return submission;
    }

    @Override
    public AssignmentSubmission evaluateSubmission(
            Long id,
            Integer marks,
            String feedback) {

        AssignmentSubmission submission =
                findEntity(id);

        if (marks == null || marks < 0) {

            throw new InvalidOperationException(
                    "Marks cannot be negative"
            );
        }

        Integer maxMarks =
                submission.getAssignment().getMaxMarks();

        if (marks > maxMarks) {

            throw new InvalidOperationException(
                    "Marks cannot exceed maximum marks: "
                            + maxMarks
            );
        }

        LocalDateTime now = LocalDateTime.now();

        submission.setMarks(marks);
        submission.setFeedback(feedback);
        submission.setStatus("EVALUATED");
        submission.setEvaluatedAt(now);

        AssignmentSubmission saved = submissionRepository.save(submission);

        // Keep the AssignmentEvaluation record (the other, previously-unsynchronized
        // source of truth for the same grade) up to date regardless of which of
        // the two evaluation APIs a caller actually used.
        AssignmentEvaluation evaluation = evaluationRepository.findBySubmissionId(id)
                .orElseGet(AssignmentEvaluation::new);
        evaluation.setSubmission(saved);
        evaluation.setMarks(marks);
        evaluation.setFeedback(feedback);
        evaluation.setEvaluatedAt(now);
        evaluationRepository.save(evaluation);

        return saved;
    }

    @Override
    public void deleteSubmission(Long id) {

        AssignmentSubmission submission =
                findEntity(id);

        submissionRepository.delete(submission);
    }
}