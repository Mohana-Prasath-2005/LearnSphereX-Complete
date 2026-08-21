package com.learnspherex.batch.controller;

import java.util.Map;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.learnspherex.batch.entity.Batch;
import com.learnspherex.batch.service.BatchService;
import com.learnspherex.common.ApiException;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    public record AnnouncementRequest(@NotBlank String message) {}

    // Broadcast an arbitrary message to every student currently enrolled in
    // this batch - a standalone action, not a side effect of some other change.
    @PostMapping("/{id}/announcements")
    public ResponseEntity<Map<String, Object>> announce(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementRequest request,
            Authentication authentication) {

        if (request.message() == null || request.message().isBlank())
            throw new ApiException(HttpStatus.BAD_REQUEST, "Announcement message is required");

        int notified = batchService.announce(id, request.message(), authentication);
        return ResponseEntity.ok(Map.of("batchId", id, "studentsNotified", notified));
    }

    @PostMapping
    public ResponseEntity<Batch> createBatch(
            @Valid @RequestBody Batch batch) {
        return ResponseEntity.ok(batchService.createBatch(batch));
    }

    @GetMapping
    public ResponseEntity<List<Batch>> getAllBatches() {
        return ResponseEntity.ok(batchService.getAllBatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Batch> getBatchById(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.getBatchById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Batch> updateBatch(
            @PathVariable Long id,
            @Valid @RequestBody Batch batch) {

        return ResponseEntity.ok(batchService.updateBatch(id, batch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBatch(@PathVariable Long id) {
        batchService.deleteBatch(id);
        return ResponseEntity.noContent().build();
    }
}