package com.learnspherex.student.repository;

import com.learnspherex.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

/** Canonical course lookup used by student enrollment. */
public interface StudentCourseCatalogRepository extends JpaRepository<Course, Long> {}
