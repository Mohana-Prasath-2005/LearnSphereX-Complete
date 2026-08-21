package com.learnspherex.batch.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.learnspherex.auth.RoleName;
import com.learnspherex.auth.User;
import com.learnspherex.auth.UserRepository;
import com.learnspherex.batch.entity.Batch;
import com.learnspherex.batch.entity.BatchStatus;
import com.learnspherex.batch.repository.BatchRepository;
import com.learnspherex.course.repository.CourseRepository;
import com.learnspherex.notification.event.NotificationEvent;

@Service
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BatchServiceImpl(
            BatchRepository batchRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher) {

        this.batchRepository = batchRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    // =========================================================
    // CREATE BATCH
    // =========================================================

    @Override
    public Batch createBatch(Batch batch) {

        // 1. Validate dates
        validateBatchDates(
                batch.getStartDate(),
                batch.getEndDate());

        // 1b. Course and trainer are mandatory regardless of which
        // controller (REST DTO validation vs. the plain web form) called in.
        if (batch.getCourseId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Course is required");
        }

        if (batch.getTrainerId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Trainer is required");
        }

        validateTrainerExists(batch.getTrainerId());

        validateNoDoubleBooking(
                batch.getTrainerId(),
                batch.getStartDate(),
                batch.getEndDate(),
                null);

        // 2. Validate course
        batch.setCourse(
                courseRepository.findById(
                        batch.getCourseId()
                ).orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Course not found with id: "
                                        + batch.getCourseId()
                        )
                )
        );

        // 3. Default status
        if (batch.getBatchStatus() == null) {
            batch.setBatchStatus(BatchStatus.PLANNED);
        }

        // 4. Save batch
        Batch savedBatch =
                batchRepository.save(batch);

        // =====================================================
        // 5. NOTIFY ASSIGNED TRAINER
        // =====================================================

        if (savedBatch.getTrainerId() != null) {

            eventPublisher.publishEvent(
                    new NotificationEvent(
                            savedBatch.getTrainerId(),
                            null,
                            "New Batch Assigned",
                            "You have been assigned to batch "
                                    + savedBatch.getBatchName()
                                    + ".",
                            "BATCH_ANNOUNCEMENT"
                    )
            );
        }

        return savedBatch;
    }


    // =========================================================
    // GET ALL BATCHES
    // =========================================================

    @Override
    public List<Batch> getAllBatches() {

        return batchRepository.findAll();
    }


    // =========================================================
    // GET BATCH BY ID
    // =========================================================

    @Override
    public Batch getBatchById(Long id) {

        return batchRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Batch not found with id: "
                                        + id
                        )
                );
    }


    // =========================================================
    // UPDATE BATCH
    // =========================================================

    @Override
    public Batch updateBatch(
            Long id,
            Batch batch) {

        Batch existingBatch =
                getBatchById(id);

        // Validate dates
        validateBatchDates(
                batch.getStartDate(),
                batch.getEndDate());

        if (batch.getCourseId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Course is required");
        }

        if (batch.getTrainerId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Trainer is required");
        }

        validateTrainerExists(batch.getTrainerId());

        validateNoDoubleBooking(
                batch.getTrainerId(),
                batch.getStartDate(),
                batch.getEndDate(),
                id);

        existingBatch.setBatchName(
                batch.getBatchName());

        existingBatch.setBatchMode(
                batch.getBatchMode());

        existingBatch.setBatchStatus(
                batch.getBatchStatus());

        existingBatch.setCapacity(
                batch.getCapacity());

        existingBatch.setStartDate(
                batch.getStartDate());

        existingBatch.setEndDate(
                batch.getEndDate());

        existingBatch.setTrainerId(
                batch.getTrainerId());

        // Update course
        existingBatch.setCourseId(
                batch.getCourseId());

        existingBatch.setCourse(
                courseRepository.findById(
                        batch.getCourseId()
                ).orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Course not found with id: "
                                        + batch.getCourseId()
                        )
                )
        );

        return batchRepository.save(existingBatch);
    }


    // =========================================================
    // DELETE BATCH
    // =========================================================

    @Override
    public void deleteBatch(Long id) {

        Batch existingBatch =
                getBatchById(id);

        batchRepository.delete(existingBatch);
    }


    // =========================================================
    // DATE VALIDATION
    // =========================================================

    private void validateBatchDates(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate != null
                && endDate != null
                && endDate.isBefore(startDate)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date cannot be before start date"
            );
        }
    }


    // =========================================================
    // TRAINER EXISTENCE VALIDATION
    // =========================================================

    private void validateTrainerExists(Long trainerId) {

        User trainer = userRepository.findByIdWithRoles(trainerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trainer not found with id: " + trainerId));

        boolean hasTrainerRole = trainer.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName() == RoleName.TRAINER);

        if (!hasTrainerRole) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User " + trainerId + " does not have the TRAINER role");
        }
    }


    // =========================================================
    // DOUBLE-BOOKING VALIDATION
    // =========================================================

    private void validateNoDoubleBooking(
            Long trainerId,
            LocalDate startDate,
            LocalDate endDate,
            Long excludeBatchId) {

        if (startDate == null || endDate == null) {
            return;
        }

        for (Batch other : batchRepository.findByTrainerId(trainerId)) {

            if (excludeBatchId != null && other.getId().equals(excludeBatchId)) {
                continue;
            }

            if (other.getBatchStatus() == BatchStatus.CANCELLED) {
                continue;
            }

            if (other.getStartDate() == null || other.getEndDate() == null) {
                continue;
            }

            boolean overlaps = !other.getStartDate().isAfter(endDate)
                    && !other.getEndDate().isBefore(startDate);

            if (overlaps) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Trainer is already assigned to batch \""
                                + other.getBatchName()
                                + "\" from " + other.getStartDate()
                                + " to " + other.getEndDate());
            }
        }
    }
}