package com.learnspherex.course.service;

import com.learnspherex.course.dto.CourseRequestDTO;
import com.learnspherex.course.dto.CourseResponseDTO;

import java.util.List;

public interface CourseService {
    CourseResponseDTO createCourse(CourseRequestDTO request);
    List<CourseResponseDTO> getAllCourses();
    CourseResponseDTO getCourseById(Long id);
    CourseResponseDTO getCourseByCode(String courseCode);
    CourseResponseDTO updateCourse(Long id, CourseRequestDTO request);
    void deleteCourse(Long id);
}