package com.learnspherex.course.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TechnologyDTO {
    private Long id;
    private String name;
    private String version;
    private String description;
}