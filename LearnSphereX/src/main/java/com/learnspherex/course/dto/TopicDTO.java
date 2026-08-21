package com.learnspherex.course.dto;

import lombok.Data;
import java.util.List;

@Data
public class TopicDTO {

    private Long id;
    private String topicName;
    private String content;
    private Integer topicOrder;

    private List<CourseMaterialDTO> materials;
}