package com.learnspherex.student.dto;

import com.learnspherex.student.entity.StudentStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentFormData {
    private Long id;
    private Long userId;
    private String studentCode;
    private LocalDate dateOfBirth;
    private String gender;
    private String qualification;
    private String college;
    private String address;
    private LocalDate joiningDate;
    private StudentStatus status = StudentStatus.ACTIVE;
}
