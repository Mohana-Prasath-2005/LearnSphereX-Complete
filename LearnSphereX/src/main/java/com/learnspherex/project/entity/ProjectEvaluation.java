package com.learnspherex.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_evaluations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", unique = true)
    private ProjectSubmission submission;

    @Column(nullable = false)
    private Long evaluatorId;

    @Column(nullable = false)
    private Integer totalMarks;

    @Column(length = 2000)
    private String feedback;

    @Column(nullable = false)
    private LocalDateTime evaluatedAt;
}
