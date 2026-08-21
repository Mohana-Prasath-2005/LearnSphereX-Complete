package com.learnspherex.project.controller;

import com.learnspherex.course.service.CourseService;
import com.learnspherex.project.dto.*;
import com.learnspherex.project.entity.ProjectStatus;
import com.learnspherex.project.service.EvaluationCriteriaService;
import com.learnspherex.project.service.ProjectEvaluationService;
import com.learnspherex.project.service.ProjectService;
import com.learnspherex.project.service.ProjectSubmissionService;
import com.learnspherex.student.service.StudentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ProjectWebController {

    private final ProjectService projectService;
    private final EvaluationCriteriaService criteriaService;
    private final ProjectSubmissionService submissionService;
    private final ProjectEvaluationService evaluationService;
    private final CourseService courseService;
    private final StudentService studentService;

    public ProjectWebController(ProjectService projectService,
                                EvaluationCriteriaService criteriaService,
                                ProjectSubmissionService submissionService,
                                ProjectEvaluationService evaluationService,
                                CourseService courseService,
                                StudentService studentService) {
        this.projectService = projectService;
        this.criteriaService = criteriaService;
        this.submissionService = submissionService;
        this.evaluationService = evaluationService;
        this.courseService = courseService;
        this.studentService = studentService;
    }

    // ---------- List ----------
    @GetMapping("/projects")
    public String list(Model model) {
        model.addAttribute("projects", projectService.findAll());
        return "project/projects";
    }

    // ---------- Create / Edit form ----------
    @GetMapping("/projects/new")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public String newForm(Model model) {
        model.addAttribute("projectForm", new ProjectFormData());
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("statuses", ProjectStatus.values());
        model.addAttribute("pageTitle", "Create Project");
        return "project/project-form";
    }

    @GetMapping("/projects/{id}/edit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public String editForm(@PathVariable Long id, Model model) {
        ProjectResponse p = projectService.findById(id);
        ProjectFormData form = new ProjectFormData();
        form.setId(p.id());
        form.setTitle(p.title());
        form.setDescription(p.description());
        form.setRequirements(p.requirements());
        form.setTechnology(p.technology());
        form.setDeadline(p.deadline());
        form.setMaximumMarks(p.maximumMarks());
        form.setTrainerId(p.trainerId());
        form.setCourseId(p.courseId());
        form.setStatus(p.status());
        model.addAttribute("projectForm", form);
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("statuses", ProjectStatus.values());
        model.addAttribute("pageTitle", "Edit Project");
        return "project/project-form";
    }

    @PostMapping("/projects/save")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public String save(@ModelAttribute ProjectFormData form, Authentication authentication) {
        ProjectRequest request = new ProjectRequest(
                form.getTitle(), form.getDescription(), form.getRequirements(), form.getTechnology(),
                form.getDeadline(), form.getMaximumMarks(), form.getTrainerId(), form.getCourseId(),
                form.getStatus());
        Long id = form.getId() == null
                ? projectService.create(request, authentication).id()
                : projectService.update(form.getId(), request, authentication).id();
        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{id}/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public String delete(@PathVariable Long id, Authentication authentication) {
        projectService.delete(id, authentication);
        return "redirect:/projects";
    }

    // ---------- Detail ----------
    @GetMapping("/projects/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        ProjectResponse project = projectService.findById(id);
        List<CriteriaResponse> criteria = criteriaService.findByProject(id);
        int criteriaTotal = criteria.stream().mapToInt(CriteriaResponse::maximumMarks).sum();

        model.addAttribute("project", project);
        model.addAttribute("criteria", criteria);
        model.addAttribute("criteriaTotal", criteriaTotal);
        model.addAttribute("submissions", submissionService.findByProject(id, authentication));
        return "project/project-details";
    }

    @GetMapping("/projects/{id}/submissions")
    public String submissionsForProject(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("project", projectService.findById(id));
        model.addAttribute("submissions", submissionService.findByProject(id, authentication));
        return "submission/submissions";
    }

    // ---------- Criteria ----------
    @GetMapping("/projects/{id}/criteria")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public String criteria(@PathVariable Long id, Model model) {
        ProjectResponse project = projectService.findById(id);
        List<CriteriaResponse> criteria = criteriaService.findByProject(id);
        int criteriaTotal = criteria.stream().mapToInt(CriteriaResponse::maximumMarks).sum();

        model.addAttribute("project", project);
        model.addAttribute("criteria", criteria);
        model.addAttribute("criteriaTotal", criteriaTotal);
        model.addAttribute("criteriaForm", new CriteriaFormData());
        return "project/criteria";
    }

    @PostMapping("/projects/{id}/criteria/save")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public String saveCriteria(@PathVariable Long id, @ModelAttribute CriteriaFormData form,
                               Authentication authentication) {
        criteriaService.create(id, new CriteriaRequest(
                form.getCriteriaName(), form.getMaximumMarks(), form.getDescription()), authentication);
        return "redirect:/projects/" + id + "/criteria";
    }

    @PostMapping("/criteria/{id}/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public String deleteCriteria(@PathVariable Long id, @RequestParam Long projectId,
                                  Authentication authentication) {
        criteriaService.delete(id, authentication);
        return "redirect:/projects/" + projectId + "/criteria";
    }

    // ---------- Submission ----------
    @GetMapping("/projects/{id}/submit")
    public String submitForm(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.findById(id));
        model.addAttribute("submissionForm", new SubmissionFormData());
        model.addAttribute("students", studentService.findAll());
        return "submission/submission-form";
    }

    @PostMapping("/projects/{id}/submit")
    public String submit(@PathVariable Long id, @ModelAttribute SubmissionFormData form,
                          Authentication authentication) {
        submissionService.submit(id, new SubmissionRequest(
                form.getGithubUrl(), form.getDeploymentUrl(), form.getDescription(),
                form.getVersion(), form.getStudentId()), authentication);
        return "redirect:/projects/" + id;
    }

    @GetMapping("/submissions/{id}")
    public String submissionDetail(@PathVariable Long id, Model model, Authentication authentication) {
        SubmissionResponse submission = submissionService.findById(id, authentication);
        model.addAttribute("submission", submission);
        try {
            model.addAttribute("evaluation", evaluationService.findBySubmission(id, authentication));
        } catch (RuntimeException notEvaluatedYet) {
            model.addAttribute("evaluation", null);
        }
        return "submission/submission-details";
    }

    // ---------- Evaluation ----------
    @GetMapping("/submissions/{id}/evaluate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER') or hasRole('EVALUATOR')")
    public String evaluateForm(@PathVariable Long id, Model model, Authentication authentication) {
        SubmissionResponse submission = submissionService.findById(id, authentication);
        ProjectResponse project = projectService.findById(submission.projectId());
        List<CriteriaResponse> criteria = criteriaService.findByProject(project.id());

        EvaluationFormData form = new EvaluationFormData();
        for (CriteriaResponse c : criteria) {
            EvaluationFormData.CriteriaScoreFormRow row = new EvaluationFormData.CriteriaScoreFormRow();
            row.setCriteriaId(c.id());
            row.setCriteriaName(c.criteriaName());
            row.setMaximumMarks(c.maximumMarks());
            form.getCriteriaScores().add(row);
        }

        model.addAttribute("submission", submission);
        model.addAttribute("projectMaxMarks", project.maximumMarks());
        model.addAttribute("evaluationForm", form);
        return "evaluation/evaluation-form";
    }

    @PostMapping("/submissions/{id}/evaluate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER') or hasRole('EVALUATOR')")
    public String evaluate(@PathVariable Long id, @ModelAttribute EvaluationFormData form,
                           Authentication authentication) {
        List<CriteriaScoreRequest> scores = form.getCriteriaScores().stream()
                .map(row -> new CriteriaScoreRequest(row.getCriteriaId(), row.getMarks(), row.getFeedback()))
                .toList();
        evaluationService.evaluate(id, new EvaluationRequest(form.getEvaluatorId(), form.getFeedback(), scores),
                authentication);
        return "redirect:/submissions/" + id;
    }
}
