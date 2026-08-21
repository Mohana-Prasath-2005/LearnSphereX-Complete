package com.learnspherex.project.dto;

import jakarta.validation.constraints.*;

public record CriteriaRequest(
        @NotBlank @Size(max = 100) String criteriaName,
        @NotNull @Min(1) Integer maximumMarks,
        @Size(max = 1000) String description) {}
