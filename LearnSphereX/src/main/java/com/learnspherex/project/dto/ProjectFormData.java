package com.learnspherex.project.dto;

import com.learnspherex.project.entity.ProjectStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectFormData {
    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String technology;
    private LocalDate deadline;
    private Integer maximumMarks;
    private Long trainerId;
    private Long courseId;
    private ProjectStatus status = ProjectStatus.ACTIVE;
}
