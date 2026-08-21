package com.learnspherex.student.dto;

public record StudentDashboardResponse(
        Long studentId,
        String studentName,
        String studentCode,
        long totalCourses,
        long activeCourses,
        long activeBatches,
        Double attendancePercentage,
        long totalAssignments,
        long completedAssignments,
        long pendingAssignments,
        long totalTests,
        Double averageTestScore,
        long totalProjects,
        long evaluatedProjects,
        Double projectAveragePercentage,
        long totalCertificates) {}
