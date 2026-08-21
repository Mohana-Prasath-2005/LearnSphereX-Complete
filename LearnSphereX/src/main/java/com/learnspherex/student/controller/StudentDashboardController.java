package com.learnspherex.student.controller;

import com.learnspherex.student.dto.StudentDashboardResponse;
import com.learnspherex.student.service.StudentDashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentDashboardController {
    private final StudentDashboardService service;

    public StudentDashboardController(StudentDashboardService service) {
        this.service = service;
    }

    @GetMapping("/{studentId}/dashboard")
    public StudentDashboardResponse dashboard(@PathVariable Long studentId, Authentication authentication) {
        return service.getDashboard(studentId, authentication);
    }
}
