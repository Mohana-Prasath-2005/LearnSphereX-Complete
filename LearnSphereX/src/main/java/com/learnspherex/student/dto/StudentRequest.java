package com.learnspherex.student.dto;

import com.learnspherex.student.entity.StudentStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record StudentRequest(
        @NotNull Long userId,
        @NotBlank @Size(max = 30) String studentCode,
        LocalDate dateOfBirth,
        @Size(max = 20) String gender,
        @NotBlank @Size(max = 100) String qualification,
        @NotBlank @Size(max = 200) String college,
        @Size(max = 500) String address,
        @NotNull LocalDate joiningDate,
        @NotNull StudentStatus status
) {}
