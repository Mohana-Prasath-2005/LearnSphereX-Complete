package com.learnspherex.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "criteria_scores", uniqueConstraints = {
        @UniqueConstraint(name = "uk_evaluation_criteria", columnNames = {"evaluation_id", "criteria_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CriteriaScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluation_id")
    private ProjectEvaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criteria_id")
    private EvaluationCriteria criteria;

    @Column(nullable = false)
    private Integer marks;

    @Column(length = 1000)
    private String feedback;
}
