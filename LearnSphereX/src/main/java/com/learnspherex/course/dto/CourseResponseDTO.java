package com.learnspherex.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDTO {

    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private String duration;
    private BigDecimal fee;
    private String prerequisites;
    private String learningObjectives;
    private String status;

    private List<CourseModuleDTO> modules;
    private List<TechnologyDTO> technologies;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}