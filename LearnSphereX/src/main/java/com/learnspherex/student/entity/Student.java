package com.learnspherex.student.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "students", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_code", columnNames = "student_code"),
        @UniqueConstraint(name = "uk_student_user", columnNames = "user_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "student_code", nullable = false, length = 30)
    private String studentCode;

    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String gender;

    @Column(nullable = false, length = 100)
    private String qualification;

    @Column(nullable = false, length = 200)
    private String college;

    @Column(length = 500)
    private String address;

    @Column(nullable = false)
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status;
}
