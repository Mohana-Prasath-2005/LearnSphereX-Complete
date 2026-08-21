package com.learnspherex.project.entity;

import com.learnspherex.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(length = 500)
    private String githubUrl;

    @Column(length = 500)
    private String deploymentUrl;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false, length = 20)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status;
}
