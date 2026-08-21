package com.learnspherex.project.controller;

import com.learnspherex.project.dto.*;
import com.learnspherex.project.service.EvaluationCriteriaService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EvaluationCriteriaController {
    private final EvaluationCriteriaService service;

    public EvaluationCriteriaController(EvaluationCriteriaService service) { this.service = service; }

    @PostMapping("/projects/{projectId}/criteria")
    public ResponseEntity<CriteriaResponse> create(@PathVariable Long projectId,
                                                     @Valid @RequestBody CriteriaRequest request,
                                                     Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(projectId, request, authentication));
    }

    @GetMapping("/projects/{projectId}/criteria")
    public java.util.List<CriteriaResponse> findByProject(@PathVariable Long projectId) {
        return service.findByProject(projectId);
    }

    @PutMapping("/criteria/{id}")
    public CriteriaResponse update(@PathVariable Long id, @Valid @RequestBody CriteriaRequest request,
                                    Authentication authentication) {
        return service.update(id, request, authentication);
    }

    @DeleteMapping("/criteria/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
