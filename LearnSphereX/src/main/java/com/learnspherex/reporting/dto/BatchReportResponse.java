package com.learnspherex.reporting.dto;

public record BatchReportResponse(
        Long batchId, String batchCode, long totalStudents,
        long activeStudents, long projectSubmissions, long evaluatedSubmissions,
        Double averageProjectPercentage) {}
