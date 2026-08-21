package com.learnspherex.assignment.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.learnspherex.assignment.entity.Assignment;
import com.learnspherex.assignment.repository.AssignmentRepository;
import com.learnspherex.assignment.repository.AssignmentSubmissionRepository;
import com.learnspherex.notification.event.NotificationEvent;
import com.learnspherex.student.entity.EnrollmentStatus;
import com.learnspherex.student.entity.StudentCourse;
import com.learnspherex.student.repository.StudentCourseRepository;

/**
 * Notifies enrolled students, once a day, about assignments due within the
 * next 24 hours that they haven't submitted yet.
 */
@Component
public class AssignmentDeadlineReminderScheduler {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AssignmentDeadlineReminderScheduler(
            AssignmentRepository assignmentRepository,
            AssignmentSubmissionRepository submissionRepository,
            StudentCourseRepository studentCourseRepository,
            ApplicationEventPublisher eventPublisher) {

        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void remindUpcomingDeadlines() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusHours(24);

        List<Assignment> assignments = assignmentRepository.findAll();

        for (Assignment assignment : assignments) {

            if (assignment.getDeadline() == null
                    || assignment.getDeadline().isBefore(now)
                    || assignment.getDeadline().isAfter(threshold)) {
                continue;
            }

            Long courseId = assignment.getCourse().getId();

            for (StudentCourse enrollment : studentCourseRepository.findByCourseId(courseId)) {

                if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
                    continue;
                }

                Long studentId = enrollment.getStudent().getId();

                boolean alreadySubmitted = !submissionRepository
                        .findByAssignmentIdAndStudentIdOrderByVersionDesc(assignment.getId(), studentId)
                        .isEmpty();

                if (!alreadySubmitted) {
                    eventPublisher.publishEvent(new NotificationEvent(
                            enrollment.getStudent().getUserId(),
                            null,
                            "Assignment Deadline",
                            "\"" + assignment.getTitle() + "\" is due on " + assignment.getDeadline(),
                            "ASSIGNMENT_DEADLINE"));
                }
            }
        }
    }
}
