package com.learnspherex.batch.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.learnspherex.batch.entity.BatchSession;
import com.learnspherex.batch.service.BatchSessionService;

@RestController
@RequestMapping("/api/batch-sessions")
public class BatchSessionController {

    private final BatchSessionService sessionService;

    public BatchSessionController(BatchSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<BatchSession> createSession(
            @Valid @RequestBody BatchSession session) {

        return ResponseEntity.ok(
                sessionService.createSession(session));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BatchSession> getSessionById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                sessionService.getSessionById(id));
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<List<BatchSession>> getSessionsByBatchId(
            @PathVariable Long batchId) {

        return ResponseEntity.ok(
                sessionService.getSessionsByBatchId(batchId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BatchSession> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody BatchSession session) {

        return ResponseEntity.ok(
                sessionService.updateSession(id, session));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable Long id) {

        sessionService.deleteSession(id);

        return ResponseEntity.noContent().build();
    }
}