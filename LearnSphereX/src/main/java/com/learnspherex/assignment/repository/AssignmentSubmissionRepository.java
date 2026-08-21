package com.learnspherex.assignment.repository;

import com.learnspherex.assignment.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentSubmissionRepository
        extends JpaRepository<AssignmentSubmission, Long> {

    List<AssignmentSubmission> findByAssignmentIdOrderByVersionDesc(
            Long assignmentId
    );

    List<AssignmentSubmission> findByStudentId(Long studentId);

    List<AssignmentSubmission> findByAssignmentIdAndStudentIdOrderByVersionDesc(
            Long assignmentId,
            Long studentId
    );
}