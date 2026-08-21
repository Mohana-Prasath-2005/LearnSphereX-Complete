package com.learnspherex.batch.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnspherex.batch.entity.BatchSession;

public interface BatchSessionRepository extends JpaRepository<BatchSession, Long> {

    List<BatchSession> findByBatchId(Long batchId);

    boolean existsByBatchIdAndSessionDate(Long batchId, LocalDate sessionDate);
}