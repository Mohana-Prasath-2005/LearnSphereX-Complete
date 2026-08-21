package com.learnspherex.assignment.service;

import com.learnspherex.assignment.entity.Assignment;

import java.util.List;

public interface AssignmentService {

    Assignment createAssignment(Assignment assignment);

    List<Assignment> getAllAssignments();

    List<Assignment> getAssignmentsByCourseId(Long courseId);

    Assignment getAssignmentById(Long id);

    Assignment updateAssignment(Long id, Assignment assignmentDetails);

    void deleteAssignment(Long id);
}