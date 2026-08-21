package com.learnspherex.project.dto;

public record CriteriaResponse(
        Long id, Long projectId, String criteriaName,
        Integer maximumMarks, String description) {}
