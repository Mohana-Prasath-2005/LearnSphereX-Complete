package com.learnspherex.project.dto;

public record CriteriaScoreResponse(
        Long criteriaId, String criteriaName, Integer maximumMarks,
        Integer marks, String feedback) {}
