package com.learnspherex.student.dto;

import com.learnspherex.student.entity.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EnrollmentRequest(
        @NotNull Long courseId,
        @NotNull Long batchId,
        @NotNull LocalDate enrollmentDate,
        @NotNull EnrollmentStatus status) {}
