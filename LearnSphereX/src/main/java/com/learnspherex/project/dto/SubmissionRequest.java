package com.learnspherex.project.dto;

import jakarta.validation.constraints.*;

public record SubmissionRequest(
        @NotBlank @Size(max = 500) String githubUrl,
        @Size(max = 500) String deploymentUrl,
        @Size(max = 2000) String description,
        @NotBlank @Size(max = 20) String version,
        @NotNull Long studentId) {}
