package com.learnspherex.student.entity;

import jakarta.persistence.*;
import com.learnspherex.course.entity.Course;
import com.learnspherex.batch.entity.Batch;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "student_courses", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_course_batch",
                columnNames = {"student_id", "course_id", "batch_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @Column(nullable = false)
    private LocalDate enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    private LocalDate completionDate;
}
