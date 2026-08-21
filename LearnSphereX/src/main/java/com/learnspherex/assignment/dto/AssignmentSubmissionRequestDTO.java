package com.learnspherex.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignmentSubmissionRequestDTO {

    @NotNull(message = "Assignment id is required")
    private Long assignmentId;

    @NotNull(message = "Student id is required")
    private Long studentId;

    @NotBlank(message = "Submission URL is required")
    private String submissionUrl;

    private String comments;
}
