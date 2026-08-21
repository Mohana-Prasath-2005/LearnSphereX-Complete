package com.learnspherex.student.service;

import com.learnspherex.exception.ResourceNotFoundException;
import com.learnspherex.student.entity.EnrollmentStatus;
import com.learnspherex.student.repository.StudentCourseRepository;
import com.learnspherex.student.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Centralises the existing course-completion definition used by downstream modules. */
@Service
public class CourseCompletionService {
    private final StudentRepository students;
    private final StudentCourseRepository enrollments;

    public CourseCompletionService(StudentRepository students, StudentCourseRepository enrollments) {
        this.students = students;
        this.enrollments = enrollments;
    }

    @Transactional(readOnly = true)
    public boolean isCourseCompleted(Long studentId, Long courseId) {
        if (!students.existsById(studentId)) throw new ResourceNotFoundException("Student not found: " + studentId);
        return enrollments.existsByStudentIdAndCourseIdAndStatus(studentId, courseId, EnrollmentStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public boolean isEnrolled(Long studentId, Long courseId) {
        return enrollments.findFirstByStudentIdAndCourseId(studentId, courseId).isPresent();
    }
}
