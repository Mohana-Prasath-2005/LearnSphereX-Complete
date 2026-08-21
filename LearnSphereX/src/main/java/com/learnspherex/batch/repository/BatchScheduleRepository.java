package com.learnspherex.batch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnspherex.batch.entity.BatchSchedule;

public interface BatchScheduleRepository
        extends JpaRepository<BatchSchedule, Long> {

    List<BatchSchedule> findByBatchId(Long batchId);
}