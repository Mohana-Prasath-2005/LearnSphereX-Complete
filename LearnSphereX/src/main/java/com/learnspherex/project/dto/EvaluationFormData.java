package com.learnspherex.project.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EvaluationFormData {
    private Long evaluatorId;
    private String feedback;
    private List<CriteriaScoreFormRow> criteriaScores = new ArrayList<>();

    @Data
    public static class CriteriaScoreFormRow {
        private Long criteriaId;
        private String criteriaName;
        private Integer maximumMarks;
        private Integer marks;
        private String feedback;
    }
}
