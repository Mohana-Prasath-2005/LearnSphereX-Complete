package com.learnspherex.course.repository;

import com.learnspherex.course.entity.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {

    List<CourseModule> findByCourseIdOrderByModuleOrderAsc(Long courseId);

    void deleteByCourseId(Long courseId);
}