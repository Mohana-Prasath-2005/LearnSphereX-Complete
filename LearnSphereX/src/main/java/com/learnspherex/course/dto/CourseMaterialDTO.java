package com.learnspherex.course.dto;

import lombok.Data;

@Data
public class CourseMaterialDTO {

    private Long id;
    private String materialTitle;
    private String materialType;
    private String fileUrl;
}