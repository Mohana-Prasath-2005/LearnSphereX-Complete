package com.learnspherex.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseMaterialRequestDTO {

    @NotBlank(message = "Material title is required")
    private String materialTitle;

    @NotBlank(message = "Material type is required")
    private String materialType; // PDF, VIDEO, LINK, DOCUMENT

    @NotBlank(message = "File URL is required")
    private String fileUrl;

    @NotNull(message = "Topic id is required")
    private Long topicId;
}
