package com.learnspherex.assignment.mapper;

import com.learnspherex.assignment.dto.AssignmentResponseDTO;
import com.learnspherex.course.dto.CourseSummaryDTO;
import com.learnspherex.assignment.entity.Assignment;

public class AssignmentMapper {

    public static AssignmentResponseDTO toDTO(Assignment assignment) {

        AssignmentResponseDTO dto = new AssignmentResponseDTO();

        dto.setId(assignment.getId());
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setDifficulty(assignment.getDifficulty());
        dto.setDeadline(assignment.getDeadline());
        dto.setMaxMarks(assignment.getMaxMarks());
        dto.setStatus(assignment.getStatus());
        dto.setCreatedAt(assignment.getCreatedAt());
        dto.setUpdatedAt(assignment.getUpdatedAt());

        // Course mapping (IMPORTANT)
        CourseSummaryDTO courseDTO = new CourseSummaryDTO();
        courseDTO.setId(assignment.getCourse().getId());
        courseDTO.setCourseCode(assignment.getCourse().getCourseCode());
        courseDTO.setCourseName(assignment.getCourse().getCourseName());

        dto.setCourse(courseDTO);

        return dto;
    }
}