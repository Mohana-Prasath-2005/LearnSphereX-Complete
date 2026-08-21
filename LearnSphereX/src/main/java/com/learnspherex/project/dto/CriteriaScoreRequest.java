package com.learnspherex.project.dto;

import jakarta.validation.constraints.*;

public record CriteriaScoreRequest(
        @NotNull Long criteriaId,
        @NotNull @Min(0) Integer marks,
        @Size(max = 1000) String feedback) {}
