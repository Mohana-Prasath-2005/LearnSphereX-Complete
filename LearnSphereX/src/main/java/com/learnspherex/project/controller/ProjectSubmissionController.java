package com.learnspherex.project.controller;

import com.learnspherex.project.dto.*;
import com.learnspherex.project.service.ProjectSubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProjectSubmissionController {
    private final ProjectSubmissionService service;

    public ProjectSubmissionController(ProjectSubmissionService service) { this.service = service; }

    @PostMapping("/projects/{projectId}/submissions")
    public ResponseEntity<SubmissionResponse> submit(@PathVariable Long projectId,
                                                      @Valid @RequestBody SubmissionRequest request,
                                                      Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(projectId, request, authentication));
    }

    @GetMapping("/students/{studentId}/submissions")
    public java.util.List<SubmissionResponse> byStudent(@PathVariable Long studentId, Authentication authentication) {
        return service.findByStudent(studentId, authentication);
    }

    @GetMapping("/projects/{projectId}/submissions")
    public java.util.List<SubmissionResponse> byProject(@PathVariable Long projectId, Authentication authentication) {
        return service.findByProject(projectId, authentication);
    }

    @GetMapping("/submissions/{id}")
    public SubmissionResponse byId(@PathVariable Long id, Authentication authentication) {
        return service.findById(id, authentication);
    }
}
