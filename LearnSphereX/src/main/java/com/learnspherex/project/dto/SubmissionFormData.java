package com.learnspherex.project.dto;

import lombok.Data;

@Data
public class SubmissionFormData {
    private Long studentId;
    private String version;
    private String githubUrl;
    private String deploymentUrl;
    private String description;
}
