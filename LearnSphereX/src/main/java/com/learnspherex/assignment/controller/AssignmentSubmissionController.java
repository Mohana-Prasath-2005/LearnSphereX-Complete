package com.learnspherex.assignment.controller;

import com.learnspherex.assignment.dto.AssignmentSubmissionRequestDTO;
import com.learnspherex.assignment.entity.Assignment;
import com.learnspherex.assignment.entity.AssignmentSubmission;
import com.learnspherex.assignment.service.AssignmentSubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignment-submissions")
public class AssignmentSubmissionController {

    private final AssignmentSubmissionService submissionService;

    public AssignmentSubmissionController(
            AssignmentSubmissionService submissionService) {

        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<AssignmentSubmission>
    createSubmission(
            @Valid @RequestBody AssignmentSubmissionRequestDTO request,
            Authentication authentication) {

        AssignmentSubmission submission = new AssignmentSubmission();
        Assignment assignment = new Assignment();
        assignment.setId(request.getAssignmentId());
        submission.setAssignment(assignment);
        submission.setStudentId(request.getStudentId());
        submission.setSubmissionUrl(request.getSubmissionUrl());
        submission.setComments(request.getComments());

        AssignmentSubmission created =
                submissionService.createSubmission(submission, authentication);

        return new ResponseEntity<>(
                created,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public ResponseEntity<List<AssignmentSubmission>>
    getSubmissionsByAssignmentId(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                submissionService
                        .getSubmissionsByAssignmentId(assignmentId)
        );
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AssignmentSubmission>>
    getSubmissionsByStudentId(
            @PathVariable Long studentId,
            Authentication authentication) {

        return ResponseEntity.ok(
                submissionService
                        .getSubmissionsByStudentId(studentId, authentication)
        );
    }

    @GetMapping("/assignment/{assignmentId}/student/{studentId}")
    public ResponseEntity<List<AssignmentSubmission>>
    getStudentSubmissionsForAssignment(
            @PathVariable Long assignmentId,
            @PathVariable Long studentId,
            Authentication authentication) {

        return ResponseEntity.ok(
                submissionService
                        .getStudentSubmissionsForAssignment(
                                assignmentId,
                                studentId,
                                authentication
                        )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentSubmission>
    getSubmissionById(@PathVariable Long id, Authentication authentication) {

        return ResponseEntity.ok(
                submissionService.getSubmissionById(id, authentication)
        );
    }

    @PutMapping("/{id}/evaluate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public ResponseEntity<AssignmentSubmission>
    evaluateSubmission(
            @PathVariable Long id,
            @RequestParam Integer marks,
            @RequestParam(required = false) String feedback) {

        return ResponseEntity.ok(
                submissionService.evaluateSubmission(
                        id,
                        marks,
                        feedback
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public ResponseEntity<Void> deleteSubmission(
            @PathVariable Long id) {

        submissionService.deleteSubmission(id);

        return ResponseEntity.noContent().build();
    }
}