package com.learnspherex.course.mapper;

import com.learnspherex.course.dto.*;
import com.learnspherex.course.entity.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class CourseMapper {

    public TechnologyDTO toTechnologyDTO(Technology entity) {
        if (entity == null) return null;
        return TechnologyDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .version(entity.getVersion())
                .description(entity.getDescription())
                .build();
    }

    public CourseMaterialDTO toMaterialDTO(CourseMaterial entity) {
        if (entity == null) return null;
        CourseMaterialDTO dto = new CourseMaterialDTO();
        dto.setId(entity.getId());
        dto.setMaterialTitle(entity.getTitle());
        dto.setMaterialType(entity.getMaterialType() != null ? entity.getMaterialType().name() : null);
        dto.setFileUrl(entity.getFileUrl());
        return dto;
    }

    public TopicDTO toTopicDTO(Topic entity) {
        if (entity == null) return null;
        TopicDTO dto = new TopicDTO();
        dto.setId(entity.getId());
        dto.setTopicName(entity.getTopicName());
        dto.setContent(entity.getDescription());
        dto.setTopicOrder(entity.getTopicOrder());
        dto.setMaterials(entity.getMaterials() != null ?
                entity.getMaterials().stream().map(this::toMaterialDTO).collect(Collectors.toList())
                : Collections.emptyList());
        return dto;
    }

    public CourseModuleDTO toModuleDTO(CourseModule entity) {
        if (entity == null) return null;
        CourseModuleDTO dto = new CourseModuleDTO();
        dto.setId(entity.getId());
        dto.setModuleName(entity.getModuleName());
        dto.setDescription(entity.getDescription());
        dto.setModuleOrder(entity.getModuleOrder());
        dto.setTopics(entity.getTopics() != null ?
                entity.getTopics().stream().map(this::toTopicDTO).collect(Collectors.toList())
                : Collections.emptyList());
        return dto;
    }

    public CourseResponseDTO toCourseResponseDTO(Course entity) {
        if (entity == null) return null;
        CourseResponseDTO dto = new CourseResponseDTO();
        dto.setId(entity.getId());
        dto.setCourseCode(entity.getCourseCode());
        dto.setCourseName(entity.getCourseName());
        dto.setDescription(entity.getDescription());
        dto.setDuration(entity.getDuration());
        dto.setFee(entity.getFee());
        dto.setPrerequisites(entity.getPrerequisites());
        dto.setLearningObjectives(entity.getLearningObjectives());
        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus().name());
        }
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setModules(entity.getModules() != null ?
                entity.getModules().stream().map(this::toModuleDTO).collect(Collectors.toList())
                : Collections.emptyList());
        dto.setTechnologies(entity.getTechnologies() != null ?
                entity.getTechnologies().stream().map(this::toTechnologyDTO).collect(Collectors.toList())
                : Collections.emptyList());
        return dto;
    }
}