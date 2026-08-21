package com.learnspherex.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evaluation_criteria", uniqueConstraints = {
        @UniqueConstraint(name = "uk_project_criteria_name", columnNames = {"project_id", "criteria_name"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EvaluationCriteria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "criteria_name", nullable = false, length = 100)
    private String criteriaName;

    @Column(nullable = false)
    private Integer maximumMarks;

    @Column(length = 1000)
    private String description;
}
