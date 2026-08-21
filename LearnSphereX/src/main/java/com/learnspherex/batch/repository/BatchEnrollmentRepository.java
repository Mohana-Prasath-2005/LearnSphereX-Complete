package com.learnspherex.batch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnspherex.batch.entity.BatchEnrollment;

public interface BatchEnrollmentRepository
        extends JpaRepository<BatchEnrollment, Long> {

    List<BatchEnrollment> findByBatchId(Long batchId);

    List<BatchEnrollment> findByStudentId(Long studentId);

    long countByBatchId(Long batchId);

    boolean existsByBatchIdAndStudentId(
            Long batchId,
            Long studentId);
}