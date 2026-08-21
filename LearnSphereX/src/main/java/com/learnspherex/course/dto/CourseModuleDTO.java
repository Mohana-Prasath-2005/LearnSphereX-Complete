package com.learnspherex.course.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseModuleDTO {

    private Long id;
    private String moduleName;
    private String description;
    private Integer moduleOrder;

    private List<TopicDTO> topics;
}
