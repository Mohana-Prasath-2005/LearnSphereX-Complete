package com.learnspherex.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseModuleRequestDTO {

    @NotBlank(message = "Module name is required")
    private String moduleName;

    private String description;

    @NotNull(message = "Module order is required")
    private Integer moduleOrder;

    @NotNull(message = "Course id is required")
    private Long courseId;
}
