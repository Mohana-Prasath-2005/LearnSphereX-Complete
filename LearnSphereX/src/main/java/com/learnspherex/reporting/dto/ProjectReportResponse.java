package com.learnspherex.reporting.dto;

public record ProjectReportResponse(
        Long projectId, String title, Integer maximumMarks,
        long totalSubmissions, long evaluatedSubmissions,
        long pendingEvaluations, Double averageMarks,
        Double averagePercentage, Integer highestMarks, Integer lowestMarks) {}
