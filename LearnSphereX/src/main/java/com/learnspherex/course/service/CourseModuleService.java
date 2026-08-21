package com.learnspherex.course.service;

import com.learnspherex.course.entity.CourseModule;

import java.util.List;

public interface CourseModuleService {

    CourseModule createModule(CourseModule module);

    List<CourseModule> getModulesByCourseId(Long courseId);

    CourseModule getModuleById(Long id);

    CourseModule updateModule(Long id, CourseModule moduleDetails);

    void deleteModule(Long id);
}