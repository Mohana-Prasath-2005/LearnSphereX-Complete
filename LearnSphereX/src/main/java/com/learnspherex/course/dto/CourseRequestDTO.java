package com.learnspherex.course.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CourseRequestDTO {

    @NotBlank(message = "Course code is required")
    private String courseCode;

    @NotBlank(message = "Course name is required")
    private String courseName;

    private String description;

    private String duration;

    @NotNull(message = "Fee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Fee cannot be negative")
    private BigDecimal fee;

    private String prerequisites;

    private String learningObjectives;

    @NotBlank(message = "Status is required")
    private String status;

    private List<Long> technologyIds;
}
