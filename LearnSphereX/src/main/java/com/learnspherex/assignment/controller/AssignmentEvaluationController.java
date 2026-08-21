package com.learnspherex.assignment.controller;

import com.learnspherex.assignment.entity.AssignmentEvaluation;
import com.learnspherex.assignment.service.AssignmentEvaluationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignment-evaluations")
public class AssignmentEvaluationController {

    private final AssignmentEvaluationService evaluationService;

    public AssignmentEvaluationController(
            AssignmentEvaluationService evaluationService) {

        this.evaluationService = evaluationService;
    }

    @PostMapping("/submission/{submissionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public ResponseEntity<AssignmentEvaluation> createEvaluation(
            @PathVariable Long submissionId,
            @RequestBody AssignmentEvaluation evaluation) {

        AssignmentEvaluation created =
                evaluationService.createEvaluation(
                        submissionId,
                        evaluation
                );

        return new ResponseEntity<>(
                created,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentEvaluation> getEvaluationById(
            @PathVariable Long id, Authentication authentication) {

        return ResponseEntity.ok(
                evaluationService.getEvaluationById(id, authentication)
        );
    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<AssignmentEvaluation>
    getEvaluationBySubmissionId(
            @PathVariable Long submissionId, Authentication authentication) {

        return ResponseEntity.ok(
                evaluationService
                        .getEvaluationBySubmissionId(submissionId, authentication)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public ResponseEntity<AssignmentEvaluation> updateEvaluation(
            @PathVariable Long id,
            @RequestBody AssignmentEvaluation evaluation) {

        return ResponseEntity.ok(
                evaluationService.updateEvaluation(
                        id,
                        evaluation
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public ResponseEntity<Void> deleteEvaluation(
            @PathVariable Long id) {

        evaluationService.deleteEvaluation(id);

        return ResponseEntity.noContent().build();
    }
}