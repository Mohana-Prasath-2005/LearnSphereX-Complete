package com.learnspherex.course.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TechnologyRequestDTO {

    @NotBlank(message = "Technology name is required")
    private String name;

    private String version;
    private String description;
}