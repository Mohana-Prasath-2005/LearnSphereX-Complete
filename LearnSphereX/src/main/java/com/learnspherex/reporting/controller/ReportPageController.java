package com.learnspherex.reporting.controller;

import com.learnspherex.batch.service.BatchService;
import com.learnspherex.course.service.CourseService;
import com.learnspherex.project.service.ProjectService;
import com.learnspherex.reporting.service.ReportService;
import com.learnspherex.student.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportPageController {

    private final ReportService reportService;
    private final StudentService studentService;
    private final BatchService batchService;
    private final CourseService courseService;
    private final ProjectService projectService;

    public ReportPageController(ReportService reportService,
                                 StudentService studentService,
                                 BatchService batchService,
                                 CourseService courseService,
                                 ProjectService projectService) {
        this.reportService = reportService;
        this.studentService = studentService;
        this.batchService = batchService;
        this.courseService = courseService;
        this.projectService = projectService;
    }

    @GetMapping("/reports")
    public String hub(Model model) {
        model.addAttribute("students", studentService.findAll());
        model.addAttribute("batches", batchService.getAllBatches());
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("projects", projectService.findAll());
        return "reports/reports";
    }

    @GetMapping("/reports/student")
    public String student(@RequestParam Long studentId, Model model, Authentication authentication) {
        model.addAttribute("student", studentService.findById(studentId, authentication));
        model.addAttribute("report", reportService.student(studentId));
        return "reports/student-report";
    }

    @GetMapping("/reports/batch")
    public String batch(@RequestParam Long batchId, Model model) {
        model.addAttribute("report", reportService.batch(batchId));
        return "reports/batch-report";
    }

    @GetMapping("/reports/course")
    public String course(@RequestParam Long courseId, Model model) {
        model.addAttribute("report", reportService.course(courseId));
        return "reports/course-report";
    }

    @GetMapping("/reports/project")
    public String project(@RequestParam Long projectId, Model model) {
        model.addAttribute("report", reportService.project(projectId));
        return "reports/project-report";
    }
}
