package com.learnspherex.course.repository;

import com.learnspherex.course.entity.Course;
import com.learnspherex.course.entity.Course.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCourseCode(String courseCode);

    Optional<Course> findByCourseCode(String courseCode);

    List<Course> findByStatus(CourseStatus status);

    List<Course> findByCourseNameContainingIgnoreCase(String name);
}