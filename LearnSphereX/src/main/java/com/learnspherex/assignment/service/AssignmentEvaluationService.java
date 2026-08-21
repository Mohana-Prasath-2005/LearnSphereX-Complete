package com.learnspherex.assignment.service;

import com.learnspherex.assignment.entity.AssignmentEvaluation;
import org.springframework.security.core.Authentication;

public interface AssignmentEvaluationService {

    AssignmentEvaluation createEvaluation(
            Long submissionId,
            AssignmentEvaluation evaluation
    );

    AssignmentEvaluation getEvaluationById(Long id, Authentication authentication);

    AssignmentEvaluation getEvaluationBySubmissionId(
            Long submissionId,
            Authentication authentication
    );

    AssignmentEvaluation updateEvaluation(
            Long id,
            AssignmentEvaluation evaluation
    );

    void deleteEvaluation(Long id);
}