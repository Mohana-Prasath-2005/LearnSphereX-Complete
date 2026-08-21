package com.learnspherex.project.service;

import com.learnspherex.exception.BadRequestException;
import com.learnspherex.exception.DuplicateResourceException;
import com.learnspherex.exception.ResourceNotFoundException;
import com.learnspherex.project.dto.*;
import com.learnspherex.project.entity.*;
import com.learnspherex.project.repository.*;
import com.learnspherex.security.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ProjectEvaluationService {
    private final ProjectEvaluationRepository evaluationRepository;
    private final CriteriaScoreRepository scoreRepository;
    private final EvaluationCriteriaService criteriaService;
    private final ProjectSubmissionService submissionService;
    private final CurrentUserService currentUserService;

    public ProjectEvaluationService(ProjectEvaluationRepository evaluationRepository,
                                    CriteriaScoreRepository scoreRepository,
                                    EvaluationCriteriaService criteriaService,
                                    ProjectSubmissionService submissionService,
                                    CurrentUserService currentUserService) {
        this.evaluationRepository = evaluationRepository;
        this.scoreRepository = scoreRepository;
        this.criteriaService = criteriaService;
        this.submissionService = submissionService;
        this.currentUserService = currentUserService;
    }

    /**
     * Grading is done either by the project's assigned trainer, an ADMIN,
     * or anyone holding the dedicated EVALUATOR role (there's no per-project
     * evaluator-assignment model in this schema, so EVALUATOR is granted broadly).
     */
    private void assertCanEvaluate(Authentication authentication, Project project) {
        if (currentUserService.hasRole(authentication, "EVALUATOR")) {
            return;
        }
        currentUserService.assertOwnerOrRole(authentication, project.getTrainerId(), "ADMIN");
    }

    public EvaluationResponse evaluate(Long submissionId, EvaluationRequest r, Authentication authentication) {
        ProjectSubmission submission = submissionService.getEntity(submissionId);
        Project project = submission.getProject();
        assertCanEvaluate(authentication, project);
        // evaluatorId must be the caller's own identity (ADMIN may record it on someone else's behalf)
        currentUserService.assertOwnerOrRole(authentication, r.evaluatorId(), "ADMIN");

        if (evaluationRepository.findBySubmissionId(submissionId).isPresent())
            throw new DuplicateResourceException("Submission is already evaluated");

        List<EvaluationCriteria> criteria = criteriaService.findByProject(project.getId()).stream()
                .map(x -> criteriaService.getEntity(x.id())).toList();

        int total = 0;

        ProjectEvaluation evaluation = ProjectEvaluation.builder()
                .submission(submission)
                .evaluatorId(r.evaluatorId())
                .totalMarks(0)
                .feedback(r.feedback())
                .evaluatedAt(LocalDateTime.now())
                .build();

        for (CriteriaScoreRequest scoreRequest : r.criteriaScores()) {
            EvaluationCriteria c = criteria.stream()
                    .filter(x -> x.getId().equals(scoreRequest.criteriaId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException(
                            "Criteria " + scoreRequest.criteriaId() + " does not belong to this project"));

            if (scoreRequest.marks() > c.getMaximumMarks())
                throw new BadRequestException("Marks cannot exceed maximum marks for " + c.getCriteriaName());

            total += scoreRequest.marks();
        }

        if (r.criteriaScores().size() != criteria.size())
            throw new BadRequestException("All evaluation criteria must be scored");

        evaluation.setTotalMarks(total);
        evaluation = evaluationRepository.save(evaluation);

        for (CriteriaScoreRequest scoreRequest : r.criteriaScores()) {
            EvaluationCriteria c = criteria.stream()
                    .filter(x -> x.getId().equals(scoreRequest.criteriaId()))
                    .findFirst().orElseThrow();
            scoreRepository.save(CriteriaScore.builder()
                    .evaluation(evaluation)
                    .criteria(c)
                    .marks(scoreRequest.marks())
                    .feedback(scoreRequest.feedback())
                    .build());
        }

        submissionService.markEvaluated(submission);

        // Same denominator as findBySubmission() below (project.maximumMarks) -
        // previously this used the sum of criteria weights instead, which could
        // differ from the project's own max and made the percentage inconsistent
        // between the create response and a later fetch of the same evaluation.
        return toResponse(evaluation, project.getMaximumMarks());
    }

    @Transactional(readOnly = true)
    public EvaluationResponse findBySubmission(Long submissionId, Authentication authentication) {
        ProjectEvaluation evaluation = evaluationRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found for submission: " + submissionId));
        ProjectSubmission submission = evaluation.getSubmission();
        if (!currentUserService.hasRole(authentication, "EVALUATOR")) {
            currentUserService.assertAnyOwnerOrRole(authentication,
                    List.of(submission.getStudent().getUserId(), submission.getProject().getTrainerId()),
                    "ADMIN");
        }
        int max = submission.getProject().getMaximumMarks();
        return toResponse(evaluation, max);
    }

    @Transactional(readOnly = true)
    public List<CriteriaScore> findScores(Long evaluationId) {
        return scoreRepository.findByEvaluationId(evaluationId);
    }

    private EvaluationResponse toResponse(ProjectEvaluation e, int maximumMarks) {
        List<CriteriaScoreResponse> scores = scoreRepository.findByEvaluationId(e.getId()).stream()
                .map(s -> new CriteriaScoreResponse(
                        s.getCriteria().getId(), s.getCriteria().getCriteriaName(),
                        s.getCriteria().getMaximumMarks(), s.getMarks(), s.getFeedback()))
                .toList();

        double percentage = maximumMarks == 0 ? 0 : e.getTotalMarks() * 100.0 / maximumMarks;
        return new EvaluationResponse(e.getId(), e.getSubmission().getId(),
                e.getEvaluatorId(), e.getTotalMarks(), maximumMarks,
                Math.round(percentage * 100.0) / 100.0, e.getFeedback(),
                e.getEvaluatedAt(), scores);
    }
}
