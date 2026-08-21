package com.learnspherex.assignment.controller;

import com.learnspherex.assignment.dto.AssignmentRequestDTO;
import com.learnspherex.assignment.dto.AssignmentResponseDTO;
import com.learnspherex.assignment.entity.Assignment;
import com.learnspherex.assignment.mapper.AssignmentMapper;
import com.learnspherex.assignment.service.AssignmentService;
import com.learnspherex.course.entity.Course;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(
            AssignmentService assignmentService) {

        this.assignmentService = assignmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public ResponseEntity<AssignmentResponseDTO> createAssignment(
            @Valid @RequestBody AssignmentRequestDTO request) {

        Assignment assignment = toEntity(request);
        Assignment created =
                assignmentService.createAssignment(assignment);

        return new ResponseEntity<>(
                AssignmentMapper.toDTO(created),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<List<AssignmentResponseDTO>> getAllAssignments() {

        return ResponseEntity.ok(
                assignmentService.getAllAssignments().stream()
                        .map(AssignmentMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<AssignmentResponseDTO>>
    getAssignmentsByCourseId(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(
                assignmentService.getAssignmentsByCourseId(courseId).stream()
                        .map(AssignmentMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponseDTO> getAssignmentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                AssignmentMapper.toDTO(assignmentService.getAssignmentById(id))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public ResponseEntity<AssignmentResponseDTO> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentRequestDTO request) {

        Assignment assignment = toEntity(request);

        return ResponseEntity.ok(
                AssignmentMapper.toDTO(
                        assignmentService.updateAssignment(id, assignment)
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id) {

        assignmentService.deleteAssignment(id);

        return ResponseEntity.noContent().build();
    }

    private Assignment toEntity(AssignmentRequestDTO request) {
        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDifficulty(request.getDifficulty());
        assignment.setDeadline(request.getDeadline());
        assignment.setMaxMarks(request.getMaxMarks());
        assignment.setStatus(request.getStatus());

        Course course = new Course();
        course.setId(request.getCourseId());
        assignment.setCourse(course);

        return assignment;
    }
}