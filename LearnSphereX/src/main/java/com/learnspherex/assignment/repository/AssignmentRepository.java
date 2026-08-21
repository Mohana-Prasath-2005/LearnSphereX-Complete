package com.learnspherex.assignment.repository;

import com.learnspherex.assignment.entity.Assignment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository
        extends JpaRepository<Assignment, Long> {

    @Query("select a from Assignment a join fetch a.course")
    List<Assignment> findAllWithCourse();

    @Query("select a from Assignment a join fetch a.course where a.id = :id")
    Optional<Assignment> findByIdWithCourse(@Param("id") Long id);

    List<Assignment> findByCourseId(Long courseId);

    List<Assignment> findByStatus(String status);
}