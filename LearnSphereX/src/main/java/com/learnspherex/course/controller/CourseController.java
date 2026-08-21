package com.learnspherex.course.controller;

import com.learnspherex.course.dto.CourseRequestDTO;
import com.learnspherex.course.dto.CourseResponseDTO;
import com.learnspherex.course.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // Create a new course
    @PostMapping
    public ResponseEntity<CourseResponseDTO> createCourse(
            @Valid @RequestBody CourseRequestDTO request) {

        CourseResponseDTO response = courseService.createCourse(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get all courses
    @GetMapping
    public ResponseEntity<List<CourseResponseDTO>> getAllCourses() {

        List<CourseResponseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    // Get course by ID
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> getCourseById(
            @PathVariable Long id) {

        CourseResponseDTO course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    // Get course by course code
    @GetMapping("/code/{courseCode}")
    public ResponseEntity<CourseResponseDTO> getCourseByCode(
            @PathVariable String courseCode) {

        CourseResponseDTO course = courseService.getCourseByCode(courseCode);
        return ResponseEntity.ok(course);
    }

    // Update course
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequestDTO request) {

        CourseResponseDTO updatedCourse = courseService.updateCourse(id, request);
        return ResponseEntity.ok(updatedCourse);
    }

    // Delete course
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long id) {

        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}