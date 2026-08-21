package com.learnspherex.student.controller;

import com.learnspherex.batch.entity.BatchStatus;
import com.learnspherex.batch.repository.BatchRepository;
import com.learnspherex.course.service.CourseService;
import com.learnspherex.project.service.ProjectSubmissionService;
import com.learnspherex.student.dto.*;
import com.learnspherex.student.service.StudentCourseService;
import com.learnspherex.student.service.StudentDashboardService;
import com.learnspherex.student.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class StudentWebController {

    private final StudentService studentService;
    private final StudentCourseService studentCourseService;
    private final StudentDashboardService dashboardService;
    private final ProjectSubmissionService submissionService;
    private final CourseService courseService;
    private final BatchRepository batchRepository;

    public StudentWebController(StudentService studentService,
                                 StudentCourseService studentCourseService,
                                 StudentDashboardService dashboardService,
                                 ProjectSubmissionService submissionService,
                                 CourseService courseService,
                                 BatchRepository batchRepository) {
        this.studentService = studentService;
        this.studentCourseService = studentCourseService;
        this.dashboardService = dashboardService;
        this.submissionService = submissionService;
        this.courseService = courseService;
        this.batchRepository = batchRepository;
    }

    public record BatchOption(Long id, String batchCode, String courseName) {}

    // ---------- List ----------
    @GetMapping("/students")
    public String list(Model model) {
        model.addAttribute("students", studentService.findAll());
        return "student/students";
    }

    // ---------- Create / Edit form ----------
    @GetMapping("/students/new")
    public String newForm(Model model) {
        model.addAttribute("studentForm", new StudentFormData());
        model.addAttribute("pageTitle", "Add Student");
        return "student/student-form";
    }

    @GetMapping("/students/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Authentication authentication) {
        StudentResponse s = studentService.findById(id, authentication);
        StudentFormData form = new StudentFormData();
        form.setId(s.id());
        form.setUserId(s.userId());
        form.setStudentCode(s.studentCode());
        form.setDateOfBirth(s.dateOfBirth());
        form.setGender(s.gender());
        form.setQualification(s.qualification());
        form.setCollege(s.college());
        form.setAddress(s.address());
        form.setJoiningDate(s.joiningDate());
        form.setStatus(s.status());
        model.addAttribute("studentForm", form);
        model.addAttribute("pageTitle", "Edit Student");
        return "student/student-form";
    }

    @PostMapping("/students/save")
    public String save(@ModelAttribute StudentFormData form, Authentication authentication) {
        StudentRequest request = new StudentRequest(form.getUserId(), form.getStudentCode(), form.getDateOfBirth(),
                form.getGender(), form.getQualification(), form.getCollege(), form.getAddress(),
                form.getJoiningDate(), form.getStatus());
        Long id = form.getId() == null
                ? studentService.create(request, authentication).id()
                : studentService.update(form.getId(), request, authentication).id();
        return "redirect:/students/" + id;
    }

    @PostMapping("/students/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication) {
        studentService.delete(id, authentication);
        return "redirect:/students";
    }

    // ---------- Detail ----------
    @GetMapping("/students/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        StudentResponse student = studentService.findById(id, authentication);
        List<StudentCourseResponse> courses = studentCourseService.findByStudent(id, authentication);
        List<com.learnspherex.project.dto.SubmissionResponse> submissions = submissionService.findByStudent(id, authentication);
        model.addAttribute("student", student);
        model.addAttribute("courses", courses);
        model.addAttribute("submissions", submissions);
        return "student/student-details";
    }

    // ---------- Dashboard ----------
    @GetMapping("/students/{id}/dashboard")
    public String dashboard(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("student", studentService.findById(id, authentication));
        model.addAttribute("dashboard", dashboardService.getDashboard(id, authentication));
        return "student/student-dashboard";
    }

    // ---------- Submissions ----------
    @GetMapping("/students/{id}/submissions")
    public String submissions(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("student", studentService.findById(id, authentication));
        model.addAttribute("submissions", submissionService.findByStudent(id, authentication));
        return "submission/submissions";
    }

    // ---------- Enrollment ----------
    @GetMapping("/students/{id}/enroll")
    public String enrollForm(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("student", studentService.findById(id, authentication));
        model.addAttribute("enrollmentForm", new EnrollmentFormData());
        model.addAttribute("courses", courseService.getAllCourses());
        List<BatchOption> batches = batchRepository.findAll().stream()
                .filter(b -> b.getBatchStatus() == BatchStatus.ACTIVE)
                .map(b -> new BatchOption(b.getId(), b.getBatchCode(),
                        courseService.getCourseById(b.getCourseId()).getCourseName()))
                .toList();
        model.addAttribute("batches", batches);
        return "student/enrollment-form";
    }

    @PostMapping("/students/{id}/enroll")
    public String enroll(@PathVariable Long id, @ModelAttribute EnrollmentFormData form,
                          Authentication authentication) {
        studentCourseService.enroll(id, new EnrollmentRequest(form.getCourseId(), form.getBatchId(),
                form.getEnrollmentDate(), form.getStatus()), authentication);
        return "redirect:/students/" + id;
    }
}
