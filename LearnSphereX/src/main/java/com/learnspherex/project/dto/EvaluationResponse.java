package com.learnspherex.project.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EvaluationResponse(
        Long id, Long submissionId, Long evaluatorId, Integer totalMarks,
        Integer maximumMarks, Double percentage, String feedback,
        LocalDateTime evaluatedAt, List<CriteriaScoreResponse> criteriaScores) {}
