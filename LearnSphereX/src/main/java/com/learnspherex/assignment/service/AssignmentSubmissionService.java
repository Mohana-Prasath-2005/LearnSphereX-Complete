package com.learnspherex.assignment.service;

import com.learnspherex.assignment.entity.AssignmentSubmission;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AssignmentSubmissionService {

    AssignmentSubmission createSubmission(
            AssignmentSubmission submission,
            Authentication authentication
    );

    List<AssignmentSubmission> getSubmissionsByAssignmentId(
            Long assignmentId
    );

    List<AssignmentSubmission> getSubmissionsByStudentId(
            Long studentId,
            Authentication authentication
    );

    List<AssignmentSubmission> getStudentSubmissionsForAssignment(
            Long assignmentId,
            Long studentId,
            Authentication authentication
    );

    AssignmentSubmission getSubmissionById(Long id, Authentication authentication);

    AssignmentSubmission evaluateSubmission(
            Long id,
            Integer marks,
            String feedback
    );

    void deleteSubmission(Long id);
}