package com.learnspherex.reporting.controller;

import com.learnspherex.reporting.dto.*;
import com.learnspherex.reporting.service.ReportService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) { this.service = service; }

    @GetMapping("/student/{studentId}")
    public StudentReportResponse student(@PathVariable Long studentId) {
        return service.student(studentId);
    }

    @GetMapping("/batch/{batchId}")
    public BatchReportResponse batch(@PathVariable Long batchId) {
        return service.batch(batchId);
    }

    @GetMapping("/course/{courseId}")
    public CourseReportResponse course(@PathVariable Long courseId) {
        return service.course(courseId);
    }

    @GetMapping("/project/{projectId}")
    public ProjectReportResponse project(@PathVariable Long projectId) {
        return service.project(projectId);
    }
}
