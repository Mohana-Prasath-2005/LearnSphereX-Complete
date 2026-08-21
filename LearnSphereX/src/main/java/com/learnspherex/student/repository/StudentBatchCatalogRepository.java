package com.learnspherex.student.repository;

import com.learnspherex.batch.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

/** Canonical batch lookup used by student enrollment. */
public interface StudentBatchCatalogRepository extends JpaRepository<Batch, Long> {}
