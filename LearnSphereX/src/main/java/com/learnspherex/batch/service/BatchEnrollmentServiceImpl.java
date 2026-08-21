package com.learnspherex.batch.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.learnspherex.batch.entity.Batch;
import com.learnspherex.batch.entity.BatchEnrollment;
import com.learnspherex.batch.repository.BatchEnrollmentRepository;
import com.learnspherex.batch.repository.BatchRepository;

@Service
public class BatchEnrollmentServiceImpl implements BatchEnrollmentService {

    private final BatchEnrollmentRepository enrollmentRepository;
    private final BatchRepository batchRepository;

    public BatchEnrollmentServiceImpl(
            BatchEnrollmentRepository enrollmentRepository,
            BatchRepository batchRepository) {

        this.enrollmentRepository = enrollmentRepository;
        this.batchRepository = batchRepository;
    }

    @Override
    public BatchEnrollment enrollStudent(BatchEnrollment enrollment) {

        // 1. Validate batch
        Batch batch = batchRepository.findById(enrollment.getBatchId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Batch not found with id: "
                                + enrollment.getBatchId()));

        // 2. Check duplicate enrollment
        boolean alreadyEnrolled =
                enrollmentRepository.existsByBatchIdAndStudentId(
                        enrollment.getBatchId(),
                        enrollment.getStudentId());

        if (alreadyEnrolled) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Student is already enrolled in this batch");
        }

        // 3. Check batch capacity
        long currentEnrollmentCount =
                enrollmentRepository.countByBatchId(
                        enrollment.getBatchId());

        if (batch.getCapacity() != null
                && currentEnrollmentCount >= batch.getCapacity()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Batch capacity is full");
        }

        // 4. Save enrollment
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public List<BatchEnrollment> getEnrollmentsByBatchId(Long batchId) {
        return enrollmentRepository.findByBatchId(batchId);
    }

    @Override
    public List<BatchEnrollment> getEnrollmentsByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    public BatchEnrollment getEnrollmentById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Enrollment not found with id: " + id));
    }

    @Override
    public BatchEnrollment updateEnrollment(
            Long id,
            BatchEnrollment enrollment) {

        BatchEnrollment existing = getEnrollmentById(id);

        existing.setBatchId(enrollment.getBatchId());
        existing.setStudentId(enrollment.getStudentId());
        existing.setEnrollmentDate(enrollment.getEnrollmentDate());
        existing.setStatus(enrollment.getStatus());

        return enrollmentRepository.save(existing);
    }

    @Override
    public void deleteEnrollment(Long id) {
        BatchEnrollment existing = getEnrollmentById(id);
        enrollmentRepository.delete(existing);
    }
}