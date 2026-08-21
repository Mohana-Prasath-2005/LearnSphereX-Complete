package com.learnspherex.assignment.dto;

import com.learnspherex.course.dto.CourseSummaryDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssignmentResponseDTO {

    private Long id;
    private String title;
    private String description;

    private CourseSummaryDTO course;

    private String difficulty;
    private LocalDateTime deadline;
    private Integer maxMarks;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}