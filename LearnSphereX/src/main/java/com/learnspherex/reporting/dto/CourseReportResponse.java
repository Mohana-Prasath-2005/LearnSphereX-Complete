package com.learnspherex.reporting.dto;

public record CourseReportResponse(
        Long courseId, String courseName, long totalStudents,
        long activeStudents, long totalProjects, long totalSubmissions,
        long evaluatedSubmissions, Double averageProjectPercentage) {}
