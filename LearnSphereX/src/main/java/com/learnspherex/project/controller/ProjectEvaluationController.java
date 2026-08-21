package com.learnspherex.project.controller;

import com.learnspherex.project.dto.*;
import com.learnspherex.project.service.ProjectEvaluationService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProjectEvaluationController {
    private final ProjectEvaluationService service;

    public ProjectEvaluationController(ProjectEvaluationService service) { this.service = service; }

    @PostMapping("/submissions/{submissionId}/evaluations")
    public ResponseEntity<EvaluationResponse> evaluate(@PathVariable Long submissionId,
                                                        @Valid @RequestBody EvaluationRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.evaluate(submissionId, request, authentication));
    }

    @GetMapping("/submissions/{submissionId}/evaluation")
    public EvaluationResponse get(@PathVariable Long submissionId, Authentication authentication) {
        return service.findBySubmission(submissionId, authentication);
    }
}
