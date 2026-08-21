package com.learnspherex.student.controller;

import com.learnspherex.student.dto.*;
import com.learnspherex.student.service.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService studentService;
    private final StudentCourseService courseService;

    public StudentController(StudentService studentService, StudentCourseService courseService) {
        this.studentService = studentService;
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request,
                                                    Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request, authentication));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public java.util.List<StudentResponse> findAll() {
        return studentService.findAll();
    }

    @GetMapping("/{id}")
    public StudentResponse findById(@PathVariable Long id, Authentication authentication) {
        return studentService.findById(id, authentication);
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest request,
                                   Authentication authentication) {
        return studentService.update(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        studentService.delete(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{studentId}/enroll")
    public ResponseEntity<StudentCourseResponse> enroll(@PathVariable Long studentId,
                                                         @Valid @RequestBody EnrollmentRequest request,
                                                         Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.enroll(studentId, request, authentication));
    }

    @GetMapping("/{studentId}/courses")
    public java.util.List<StudentCourseResponse> courses(@PathVariable Long studentId, Authentication authentication) {
        return courseService.findByStudent(studentId, authentication);
    }
}
