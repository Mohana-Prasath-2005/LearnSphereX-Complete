package com.learnspherex.student.dto;

import com.learnspherex.student.entity.EnrollmentStatus;
import java.time.LocalDate;

public record StudentCourseResponse(
        Long id, Long studentId, Long courseId, String courseName,
        Long batchId, String batchCode, LocalDate enrollmentDate,
        EnrollmentStatus status, LocalDate completionDate) {}
