package com.learnspherex.examination.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.learnspherex.examination.entity.ExamAttempt;
import com.learnspherex.examination.repository.ExamAttemptRepository;
import com.learnspherex.examination.service.ExamService;

/**
 * Finalizes exam attempts whose per-attempt duration has run out but that the
 * student never explicitly submitted (crashed browser, closed tab, gave up).
 * Without this, such attempts would sit as STARTED/unsubmitted forever.
 */
@Component
public class ExamAttemptAutoSubmitScheduler {

    private final ExamAttemptRepository attemptRepository;
    private final ExamService examService;

    public ExamAttemptAutoSubmitScheduler(
            ExamAttemptRepository attemptRepository,
            ExamService examService) {

        this.attemptRepository = attemptRepository;
        this.examService = examService;
    }

    // Not read-only: this method's own lazy-loading needs an open session, and it
    // indirectly triggers real writes via examService.autoSubmit(...) below.
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoSubmitExpiredAttempts() {

        LocalDateTime now = LocalDateTime.now();

        List<ExamAttempt> unsubmitted =
                attemptRepository.findBySubmittedFalse();

        for (ExamAttempt attempt : unsubmitted) {

            LocalDateTime expiresAt = attempt.getStartedAt()
                    .plusMinutes(attempt.getExam().getDurationMinutes());

            if (now.isAfter(expiresAt)) {
                examService.autoSubmit(attempt.getId());
            }
        }
    }
}
