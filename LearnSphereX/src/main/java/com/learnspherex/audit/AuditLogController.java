package com.learnspherex.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// The audit trail was write-only: entries were recorded but nothing could ever read them back.
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository repository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLog> all() {
        return repository.findAllByOrderByTimestampDesc();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLog> byUser(@PathVariable Long userId) {
        return repository.findByUserIdOrderByTimestampDesc(userId);
    }
}
