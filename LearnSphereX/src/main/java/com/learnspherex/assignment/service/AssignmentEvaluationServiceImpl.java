package com.learnspherex.assignment.service;

import com.learnspherex.assignment.entity.AssignmentEvaluation;
import com.learnspherex.assignment.entity.AssignmentSubmission;
import com.learnspherex.assignment.repository.AssignmentEvaluationRepository;
import com.learnspherex.assignment.repository.AssignmentSubmissionRepository;
import com.learnspherex.exception.DuplicateResourceException;
import com.learnspherex.exception.InvalidOperationException;
import com.learnspherex.exception.ResourceNotFoundException;
import com.learnspherex.security.CurrentUserService;
import com.learnspherex.student.entity.Student;
import com.learnspherex.student.repository.StudentRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssignmentEvaluationServiceImpl
        implements AssignmentEvaluationService {

    private final AssignmentEvaluationRepository evaluationRepository;

    private final AssignmentSubmissionRepository submissionRepository;
    private final CurrentUserService currentUserService;
    private final StudentRepository studentRepository;

    public AssignmentEvaluationServiceImpl(
            AssignmentEvaluationRepository evaluationRepository,
            AssignmentSubmissionRepository submissionRepository,
            CurrentUserService currentUserService,
            StudentRepository studentRepository) {

        this.evaluationRepository = evaluationRepository;
        this.submissionRepository = submissionRepository;
        this.currentUserService = currentUserService;
        this.studentRepository = studentRepository;
    }

    private void assertOwnerOrStaff(Authentication authentication, Long studentId) {
        Long ownerUserId = studentRepository.findById(studentId).map(Student::getUserId).orElse(null);
        currentUserService.assertOwnerOrRole(authentication, ownerUserId, "ADMIN", "TRAINER");
    }

    // Create evaluation for a submission
    @Override
    public AssignmentEvaluation createEvaluation(
            Long submissionId,
            AssignmentEvaluation evaluation) {

        AssignmentSubmission submission =
                submissionRepository.findById(submissionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Submission not found with id: "
                                                + submissionId
                                )
                        );

        if (evaluationRepository.findBySubmissionId(submissionId).isPresent()) {
            throw new DuplicateResourceException(
                    "Submission " + submissionId + " has already been evaluated");
        }

        validateMarks(evaluation.getMarks(), submission);

        // Connect evaluation with submission
        evaluation.setSubmission(submission);

        // Save evaluation first.
        // @PrePersist in AssignmentEvaluation
        // automatically sets evaluatedAt.
        AssignmentEvaluation savedEvaluation =
                evaluationRepository.save(evaluation);

        // Update submission with evaluation details
        submission.setMarks(
                savedEvaluation.getMarks()
        );

        submission.setFeedback(
                savedEvaluation.getFeedback()
        );

        submission.setStatus("EVALUATED");

        submission.setEvaluatedAt(
                savedEvaluation.getEvaluatedAt()
        );

        // Persist explicitly instead of relying on implicit dirty-checking flush
        submissionRepository.save(submission);

        return savedEvaluation;
    }

    private void validateMarks(Integer marks, AssignmentSubmission submission) {
        if (marks == null || marks < 0) {
            throw new InvalidOperationException("Marks cannot be negative");
        }

        Integer maxMarks = submission.getAssignment().getMaxMarks();

        if (marks > maxMarks) {
            throw new InvalidOperationException(
                    "Marks cannot exceed maximum marks: " + maxMarks
            );
        }
    }

    private AssignmentEvaluation findEntity(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Evaluation not found with id: " + id
                        )
                );
    }

    // Get evaluation by ID
    @Override
    @Transactional(readOnly = true)
    public AssignmentEvaluation getEvaluationById(Long id, Authentication authentication) {

        AssignmentEvaluation evaluation = findEntity(id);
        assertOwnerOrStaff(authentication, evaluation.getSubmission().getStudentId());
        return evaluation;
    }

    // Get evaluation by submission ID
    @Override
    @Transactional(readOnly = true)
    public AssignmentEvaluation getEvaluationBySubmissionId(
            Long submissionId, Authentication authentication) {

        AssignmentEvaluation evaluation = evaluationRepository
                .findBySubmissionId(submissionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Evaluation not found for submission id: "
                                        + submissionId
                        )
                );
        assertOwnerOrStaff(authentication, evaluation.getSubmission().getStudentId());
        return evaluation;
    }

    // Update evaluation
    @Override
    public AssignmentEvaluation updateEvaluation(
            Long id,
            AssignmentEvaluation evaluation) {

        AssignmentEvaluation existingEvaluation =
                findEntity(id);

        // Get the submission already connected
        // to this evaluation
        AssignmentSubmission submission =
                existingEvaluation.getSubmission();

        if (submission != null) {
            validateMarks(evaluation.getMarks(), submission);
        }

        // Update marks
        existingEvaluation.setMarks(
                evaluation.getMarks()
        );

        // Update feedback
        existingEvaluation.setFeedback(
                evaluation.getFeedback()
        );

        if (submission != null) {

            submission.setMarks(
                    existingEvaluation.getMarks()
            );

            submission.setFeedback(
                    existingEvaluation.getFeedback()
            );

            submission.setStatus("EVALUATED");

            submission.setEvaluatedAt(
                    existingEvaluation.getEvaluatedAt()
            );

            // Persist explicitly instead of relying on implicit dirty-checking flush
            submissionRepository.save(submission);
        }

        return evaluationRepository.save(
                existingEvaluation
        );
    }

    // Delete evaluation
    @Override
    public void deleteEvaluation(Long id) {

        AssignmentEvaluation evaluation =
                findEntity(id);

        evaluationRepository.delete(evaluation);
    }
}