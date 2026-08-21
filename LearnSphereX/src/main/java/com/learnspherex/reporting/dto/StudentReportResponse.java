package com.learnspherex.reporting.dto;

public record StudentReportResponse(
        Long studentId, String studentCode, long totalCourses,
        long activeCourses, long totalProjects, long evaluatedProjects,
        Double projectAveragePercentage, Double overallPerformancePercentage) {}
