package com.learnspherex.assignment.repository;

import com.learnspherex.assignment.entity.Assignment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository
        extends JpaRepository<Assignment, Long> {

    @Query("select a from Assignment a join fetch a.course")
    List<Assignment> findAllWithCourse();

    List<Assignment> findByCourseId(Long courseId);

    List<Assignment> findByStatus(String status);
}