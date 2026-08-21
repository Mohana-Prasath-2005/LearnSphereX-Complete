package com.learnspherex.project.controller;

import com.learnspherex.project.dto.*;
import com.learnspherex.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService service;

    public ProjectController(ProjectService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request,
                                                    Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, authentication));
    }

    @GetMapping
    public java.util.List<ProjectResponse> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ProjectResponse findById(@PathVariable Long id) { return service.findById(id); }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request,
                                   Authentication authentication) {
        return service.update(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
