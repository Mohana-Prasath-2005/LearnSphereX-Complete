package com.learnspherex.project.dto;

import com.learnspherex.project.entity.ProjectStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ProjectRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 2000) String description,
        @Size(max = 3000) String requirements,
        @Size(max = 500) String technology,
        @NotNull LocalDate deadline,
        @NotNull @Min(1) Integer maximumMarks,
        @NotNull Long trainerId,
        @NotNull Long courseId,
        @NotNull ProjectStatus status) {}
