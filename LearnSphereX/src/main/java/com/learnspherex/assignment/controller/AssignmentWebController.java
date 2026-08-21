package com.learnspherex.assignment.controller;

import com.learnspherex.assignment.entity.Assignment;
import com.learnspherex.assignment.entity.AssignmentSubmission;
import com.learnspherex.assignment.service.AssignmentService;
import com.learnspherex.assignment.service.AssignmentSubmissionService;
import com.learnspherex.course.entity.Course;
import com.learnspherex.course.service.CourseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
public class AssignmentWebController {

    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final AssignmentSubmissionService submissionService;

    public AssignmentWebController(AssignmentService assignmentService,
                                    CourseService courseService,
                                    AssignmentSubmissionService submissionService) {
        this.assignmentService = assignmentService;
        this.courseService = courseService;
        this.submissionService = submissionService;
    }

    // ---------- List ----------
    @GetMapping("/assignments")
    public String viewAssignmentsPage(Model model) {
        model.addAttribute("assignments", assignmentService.getAllAssignments());
        return "assignments";
    }

    // ---------- Add ----------
    @GetMapping("/assignments/new")
    public String newAssignmentForm(Model model) {
        model.addAttribute("assignment", new Assignment());
        model.addAttribute("courses", courseService.getAllCourses());
        return "assignment-form";
    }

    @PostMapping("/assignments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public String createAssignment(@RequestParam String title,
                                    @RequestParam(required = false) String description,
                                    @RequestParam Long courseId,
                                    @RequestParam String difficulty,
                                    @RequestParam String deadline,
                                    @RequestParam Integer maxMarks,
                                    @RequestParam String status,
                                    Model model) {
        try {
            Assignment assignment = new Assignment();
            assignment.setTitle(title);
            assignment.setDescription(description);
            Course course = new Course();
            course.setId(courseId);
            assignment.setCourse(course);
            assignment.setDifficulty(difficulty);
            assignment.setDeadline(LocalDateTime.parse(deadline));
            assignment.setMaxMarks(maxMarks);
            assignment.setStatus(status);
            assignmentService.createAssignment(assignment);
            return "redirect:/assignments";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("assignment", new Assignment());
            model.addAttribute("courses", courseService.getAllCourses());
            return "assignment-form";
        }
    }

    // ---------- Edit ----------
    @GetMapping("/assignments/{id}/edit")
    public String editAssignmentForm(@PathVariable Long id, Model model) {
        model.addAttribute("assignment", assignmentService.getAssignmentById(id));
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("editing", true);
        return "assignment-form";
    }

    @PostMapping("/assignments/{id}/edit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public String updateAssignment(@PathVariable Long id,
                                    @RequestParam String title,
                                    @RequestParam(required = false) String description,
                                    @RequestParam Long courseId,
                                    @RequestParam String difficulty,
                                    @RequestParam String deadline,
                                    @RequestParam Integer maxMarks,
                                    @RequestParam String status,
                                    Model model) {
        try {
            Assignment details = new Assignment();
            details.setTitle(title);
            details.setDescription(description);
            Course course = new Course();
            course.setId(courseId);
            details.setCourse(course);
            details.setDifficulty(difficulty);
            details.setDeadline(LocalDateTime.parse(deadline));
            details.setMaxMarks(maxMarks);
            details.setStatus(status);
            assignmentService.updateAssignment(id, details);
            return "redirect:/assignments/" + id;
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("assignment", assignmentService.getAssignmentById(id));
            model.addAttribute("courses", courseService.getAllCourses());
            model.addAttribute("editing", true);
            return "assignment-form";
        }
    }

    // ---------- Delete ----------
    @PostMapping("/assignments/{id}/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public String deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return "redirect:/assignments";
    }

    // ---------- Detail (with submissions) ----------
    @GetMapping("/assignments/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public String assignmentDetail(@PathVariable Long id, Model model) {
        model.addAttribute("assignment", assignmentService.getAssignmentById(id));
        model.addAttribute(
                "submissions",
                submissionService.getSubmissionsByAssignmentId(id)
        );
        return "assignment-detail";
    }

    // ---------- Student submits ----------
    @PostMapping("/assignments/{id}/submit")
    public String submitAssignment(@PathVariable Long id,
                                    @RequestParam Long studentId,
                                    @RequestParam String submissionUrl,
                                    @RequestParam(required = false) String comments,
                                    Authentication authentication) {
        AssignmentSubmission submission = new AssignmentSubmission();
        Assignment assignment = new Assignment();
        assignment.setId(id);
        submission.setAssignment(assignment);
        submission.setStudentId(studentId);
        submission.setSubmissionUrl(submissionUrl);
        submission.setComments(comments);
        submissionService.createSubmission(submission, authentication);
        return "redirect:/assignments/" + id;
    }

    // ---------- Trainer evaluates ----------
    @PostMapping("/submissions/{submissionId}/evaluate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public String evaluateSubmission(@PathVariable Long submissionId,
                                      @RequestParam Long assignmentId,
                                      @RequestParam Integer marks,
                                      @RequestParam(required = false) String feedback) {
        submissionService.evaluateSubmission(submissionId, marks, feedback);
        return "redirect:/assignments/" + assignmentId;
    }
}