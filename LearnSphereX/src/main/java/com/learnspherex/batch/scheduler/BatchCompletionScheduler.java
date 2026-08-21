package com.learnspherex.batch.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.learnspherex.batch.entity.Batch;
import com.learnspherex.batch.entity.BatchStatus;
import com.learnspherex.batch.repository.BatchRepository;
import com.learnspherex.student.entity.EnrollmentStatus;
import com.learnspherex.student.entity.StudentCourse;
import com.learnspherex.student.repository.StudentCourseRepository;

@Component
public class BatchCompletionScheduler {

    private final BatchRepository batchRepository;
    private final StudentCourseRepository studentCourseRepository;

    public BatchCompletionScheduler(
            BatchRepository batchRepository,
            StudentCourseRepository studentCourseRepository) {

        this.batchRepository = batchRepository;
        this.studentCourseRepository = studentCourseRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void completeExpiredBatches() {

        LocalDate today = LocalDate.now();

        List<Batch> batches =
                batchRepository.findAll();

        for (Batch batch : batches) {

            if (batch.getEndDate() != null
                    && batch.getEndDate()
                            .isBefore(today)
                    && batch.getBatchStatus()
                            != BatchStatus.COMPLETED
                    && batch.getBatchStatus()
                            != BatchStatus.CANCELLED) {

                batch.setBatchStatus(
                        BatchStatus.COMPLETED);

                batchRepository.save(batch);

                // Certificate eligibility is checked against StudentCourse's
                // enrollment status, an entirely separate model from
                // Batch.batchStatus - without this, completing a batch had
                // zero effect on whether its students could get certified.
                completeEnrollmentsForBatch(batch, today);
            }
        }
    }

    private void completeEnrollmentsForBatch(Batch batch, LocalDate today) {

        List<StudentCourse> enrollments =
                studentCourseRepository.findByBatchId(batch.getId());

        for (StudentCourse enrollment : enrollments) {

            if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {

                enrollment.setStatus(EnrollmentStatus.COMPLETED);
                enrollment.setCompletionDate(today);

                studentCourseRepository.save(enrollment);
            }
        }
    }
}