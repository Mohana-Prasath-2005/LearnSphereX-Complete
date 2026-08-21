package com.learnspherex.student.dto;

import com.learnspherex.student.entity.StudentStatus;
import java.time.LocalDate;

public record StudentResponse(
        Long id, Long userId, String studentCode, LocalDate dateOfBirth,
        String gender, String qualification, String college, String address,
        LocalDate joiningDate, StudentStatus status) {}
