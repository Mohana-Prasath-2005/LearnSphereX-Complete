package com.learnspherex.audit;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
	List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);
	List<AuditLog> findAllByOrderByTimestampDesc();
}
