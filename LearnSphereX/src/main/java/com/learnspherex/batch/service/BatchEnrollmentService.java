package com.learnspherex.batch.service;

import java.util.List;

import com.learnspherex.batch.entity.BatchEnrollment;

public interface BatchEnrollmentService {

    BatchEnrollment enrollStudent(BatchEnrollment enrollment);

    List<BatchEnrollment> getEnrollmentsByBatchId(Long batchId);

    List<BatchEnrollment> getEnrollmentsByStudentId(Long studentId);

    BatchEnrollment getEnrollmentById(Long id);

    BatchEnrollment updateEnrollment(Long id, BatchEnrollment enrollment);

    void deleteEnrollment(Long id);
}