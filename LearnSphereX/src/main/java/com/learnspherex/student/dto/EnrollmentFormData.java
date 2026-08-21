package com.learnspherex.student.dto;

import com.learnspherex.student.entity.EnrollmentStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EnrollmentFormData {
    private Long courseId;
    private Long batchId;
    private LocalDate enrollmentDate = LocalDate.now();
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;
}
