package com.learnspherex.project.dto;

import com.learnspherex.project.entity.ProjectStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectResponse(
        Long id, String title, String description, String requirements,
        String technology, LocalDate deadline, Integer maximumMarks,
        LocalDateTime createdAt, Long trainerId, Long courseId,
        String courseName, ProjectStatus status) {}
