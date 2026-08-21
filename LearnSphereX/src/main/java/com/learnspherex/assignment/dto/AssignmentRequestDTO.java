package com.learnspherex.assignment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AssignmentRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Course id is required")
    private Long courseId;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be in the future")
    private LocalDateTime deadline;

    @NotNull(message = "Max marks is required")
    @Positive(message = "Max marks must be greater than 0")
    private Integer maxMarks;

    @NotBlank(message = "Status is required")
    private String status;
}