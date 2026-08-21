package com.learnspherex.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record EvaluationRequest(
        @NotNull Long evaluatorId,
        @Size(max = 2000) String feedback,
        @NotEmpty List<@Valid CriteriaScoreRequest> criteriaScores) {}
