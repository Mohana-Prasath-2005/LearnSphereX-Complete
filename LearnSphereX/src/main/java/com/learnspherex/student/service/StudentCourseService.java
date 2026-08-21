package com.learnspherex.student.service;

import com.learnspherex.exception.DuplicateResourceException;
import com.learnspherex.exception.InvalidOperationException;
import com.learnspherex.exception.ResourceNotFoundException;
import com.learnspherex.student.dto.*;
import com.learnspherex.student.entity.*;
import com.learnspherex.student.repository.*;
import com.learnspherex.course.repository.CourseRepository;
import com.learnspherex.batch.repository.BatchRepository;
import com.learnspherex.course.entity.Course;
import com.learnspherex.batch.entity.Batch;
import com.learnspherex.security.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentCourseService {
    private final StudentCourseRepository repository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final CurrentUserService currentUserService;

    public StudentCourseService(StudentCourseRepository repository,
                                StudentRepository studentRepository,
                                CourseRepository courseRepository,
                                BatchRepository batchRepository,
                                CurrentUserService currentUserService) {
        this.repository = repository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.batchRepository = batchRepository;
        this.currentUserService = currentUserService;
    }

    public StudentCourseResponse enroll(Long studentId, EnrollmentRequest request, Authentication authentication) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        currentUserService.assertOwnerOrRole(authentication, student.getUserId(), "ADMIN");
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + request.courseId()));
        Batch batch = batchRepository.findById(request.batchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + request.batchId()));

        if (!batch.isActive())
            throw new InvalidOperationException("Batch is not active");
        if (!batch.getCourse().getId().equals(course.getId()))
            throw new InvalidOperationException("Batch does not belong to the selected course");
        if (repository.existsByStudentIdAndCourseIdAndBatchId(studentId, course.getId(), batch.getId()))
            throw new DuplicateResourceException("Student is already enrolled in this course and batch");

        StudentCourse enrollment = StudentCourse.builder()
                .student(student).course(course).batch(batch)
                .enrollmentDate(request.enrollmentDate())
                .status(request.status()).build();

        return toResponse(repository.save(enrollment));
    }

    @Transactional(readOnly = true)
    public List<StudentCourseResponse> findByStudent(Long studentId, Authentication authentication) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        currentUserService.assertOwnerOrRole(authentication, student.getUserId(), "ADMIN", "TRAINER");
        return repository.findByStudentId(studentId).stream().map(this::toResponse).toList();
    }

    private StudentCourseResponse toResponse(StudentCourse e) {
        return new StudentCourseResponse(e.getId(), e.getStudent().getId(),
                e.getCourse().getId(), e.getCourse().getCourseName(),
                e.getBatch().getId(), e.getBatch().getBatchCode(),
                e.getEnrollmentDate(), e.getStatus(), e.getCompletionDate());
    }
}
