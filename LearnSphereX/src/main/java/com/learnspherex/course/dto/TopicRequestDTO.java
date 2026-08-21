package com.learnspherex.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopicRequestDTO {

    @NotBlank(message = "Topic name is required")
    private String topicName;

    private String content;

    @NotNull(message = "Topic order is required")
    private Integer topicOrder;

    @NotNull(message = "Module id is required")
    private Long moduleId;
}
