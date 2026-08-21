package com.learnspherex.project.dto;

import com.learnspherex.project.entity.SubmissionStatus;
import java.time.LocalDateTime;

public record SubmissionResponse(
        Long id, Long projectId, String projectTitle, Long studentId,
        String studentCode, String githubUrl, String deploymentUrl,
        String description, LocalDateTime submittedAt, String version,
        SubmissionStatus status) {}
