package com.learnspherex.batch.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.learnspherex.batch.entity.Batch;
import com.learnspherex.batch.entity.BatchEnrollment;
import com.learnspherex.batch.entity.BatchSchedule;
import com.learnspherex.batch.entity.BatchSession;
import com.learnspherex.batch.entity.Holiday;
import com.learnspherex.batch.repository.BatchEnrollmentRepository;
import com.learnspherex.batch.repository.BatchRepository;
import com.learnspherex.batch.repository.BatchScheduleRepository;
import com.learnspherex.batch.repository.BatchSessionRepository;
import com.learnspherex.batch.repository.HolidayRepository;
import com.learnspherex.notification.event.NotificationEvent;

@Service
public class BatchScheduleServiceImpl implements BatchScheduleService {

    private final BatchScheduleRepository scheduleRepository;

    private final BatchRepository batchRepository;

    private final BatchEnrollmentRepository enrollmentRepository;

    private final BatchSessionRepository sessionRepository;

    private final HolidayRepository holidayRepository;

    private final ApplicationEventPublisher eventPublisher;

    public BatchScheduleServiceImpl(
            BatchScheduleRepository scheduleRepository,
            BatchRepository batchRepository,
            BatchEnrollmentRepository enrollmentRepository,
            BatchSessionRepository sessionRepository,
            HolidayRepository holidayRepository,
            ApplicationEventPublisher eventPublisher) {

        this.scheduleRepository = scheduleRepository;
        this.batchRepository = batchRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.sessionRepository = sessionRepository;
        this.holidayRepository = holidayRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public BatchSchedule createSchedule(BatchSchedule schedule) {

        // ==========================================
        // 1. CHECK WHETHER BATCH EXISTS
        // ==========================================

        Batch batch = batchRepository.findById(schedule.getBatchId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Batch not found with id: "
                                        + schedule.getBatchId()
                        ));

        // ==========================================
        // 2. VALIDATE TIME
        // ==========================================

        validateScheduleTime(schedule);

        // ==========================================
        // 3. SAVE SCHEDULE
        // ==========================================

        BatchSchedule savedSchedule =
                scheduleRepository.save(schedule);

        // ==========================================
        // 3b. GENERATE THE ACTUAL DATED SESSIONS
        // ==========================================
        // A schedule ("every Monday 9-11am") is just a recurring template;
        // without this, nothing ever turns it into real BatchSession rows.

        generateSessionsForSchedule(batch, savedSchedule);

        // ==========================================
        // 4. FIND STUDENTS IN THIS BATCH
        // ==========================================

        List<BatchEnrollment> enrollments =
                enrollmentRepository.findByBatchId(
                        schedule.getBatchId());

        // ==========================================
        // 5. SEND NOTIFICATION TO EACH STUDENT
        // ==========================================

        for (BatchEnrollment enrollment : enrollments) {

            // Only notify active/enrolled students
            if (enrollment.getStatus() != null
                    && enrollment.getStatus()
                            .equalsIgnoreCase("ACTIVE")) {

                Long studentId =
                        enrollment.getStudentId();

                String title =
                        "New Session Scheduled";

                String message =
                        "A new session has been scheduled "
                        + "for your batch. "
                        + "Day: "
                        + schedule.getDayOfWeek()
                        + ", Time: "
                        + schedule.getStartTime()
                        + " - "
                        + schedule.getEndTime();

                NotificationEvent event =
                        new NotificationEvent(
                                studentId,
                                null,
                                title,
                                message,
                                "SCHEDULE"
                        );

                eventPublisher.publishEvent(event);
            }
        }

        return savedSchedule;
    }

    @Override
    public List<BatchSchedule> getSchedulesByBatchId(
            Long batchId) {

        batchRepository.findById(batchId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Batch not found with id: "
                                        + batchId
                        ));

        return scheduleRepository.findByBatchId(batchId);
    }

    @Override
    public List<BatchSchedule> getAllSchedules() {

        return scheduleRepository.findAll();
    }

    @Override
    public BatchSchedule getScheduleById(Long id) {

        return scheduleRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Schedule not found with id: "
                                        + id
                        ));
    }

    @Override
    public BatchSchedule updateSchedule(
            Long id,
            BatchSchedule schedule) {

        BatchSchedule existing =
                getScheduleById(id);

        // ==========================================
        // CHECK NEW BATCH
        // ==========================================

        batchRepository.findById(
                schedule.getBatchId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Batch not found with id: "
                                        + schedule.getBatchId()
                        ));

        // ==========================================
        // VALIDATE TIME
        // ==========================================

        validateScheduleTime(schedule);

        // ==========================================
        // UPDATE
        // ==========================================

        existing.setBatchId(
                schedule.getBatchId());

        existing.setDayOfWeek(
                schedule.getDayOfWeek());

        existing.setStartTime(
                schedule.getStartTime());

        existing.setEndTime(
                schedule.getEndTime());

        return scheduleRepository.save(existing);
    }

    @Override
    public void deleteSchedule(Long id) {

        BatchSchedule existing =
                getScheduleById(id);

        scheduleRepository.delete(existing);
    }

    // ==========================================
    // GENERATE SESSIONS FROM A RECURRING SCHEDULE
    // ==========================================

    private void generateSessionsForSchedule(Batch batch, BatchSchedule schedule) {

        if (batch.getStartDate() == null
                || batch.getEndDate() == null
                || schedule.getDayOfWeek() == null) {
            return;
        }

        Set<LocalDate> holidayDates = holidayRepository
                .findByBatchIdOrBatchIdIsNull(batch.getId())
                .stream()
                .map(Holiday::getHolidayDate)
                .collect(Collectors.toCollection(HashSet::new));

        LocalDate date = batch.getStartDate();

        while (date.getDayOfWeek() != schedule.getDayOfWeek()
                && !date.isAfter(batch.getEndDate())) {
            date = date.plusDays(1);
        }

        for (; !date.isAfter(batch.getEndDate()); date = date.plusWeeks(1)) {

            if (holidayDates.contains(date)) {
                continue;
            }

            if (sessionRepository.existsByBatchIdAndSessionDate(batch.getId(), date)) {
                continue;
            }

            BatchSession session = new BatchSession();
            session.setBatchId(batch.getId());
            session.setSessionDate(date);
            session.setStartTime(schedule.getStartTime());
            session.setEndTime(schedule.getEndTime());
            session.setTopic("Scheduled session (" + schedule.getDayOfWeek() + ")");
            session.setStatus("SCHEDULED");

            sessionRepository.save(session);
        }
    }


    // ==========================================
    // TIME VALIDATION
    // ==========================================

    private void validateScheduleTime(
            BatchSchedule schedule) {

        if (schedule.getStartTime() != null
                && schedule.getEndTime() != null
                && !schedule.getEndTime()
                        .isAfter(
                                schedule.getStartTime())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End time must be after start time"
            );
        }
    }
}