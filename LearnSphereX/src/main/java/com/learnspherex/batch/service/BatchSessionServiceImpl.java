package com.learnspherex.batch.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.learnspherex.batch.entity.Batch;
import com.learnspherex.batch.entity.BatchSession;
import com.learnspherex.batch.repository.BatchRepository;
import com.learnspherex.batch.repository.BatchSessionRepository;

@Service
public class BatchSessionServiceImpl implements BatchSessionService {

    private final BatchSessionRepository sessionRepository;
    private final BatchRepository batchRepository;

    public BatchSessionServiceImpl(
            BatchSessionRepository sessionRepository,
            BatchRepository batchRepository) {

        this.sessionRepository = sessionRepository;
        this.batchRepository = batchRepository;
    }

    // ==========================================
    // CREATE SESSION
    // ==========================================

    @Override
    public BatchSession createSession(
            BatchSession session) {

        // Check whether the batch exists
        Batch batch = batchRepository.findById(
                session.getBatchId()
        ).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Batch not found with id: "
                                + session.getBatchId()
                )
        );

        // Validate session time
        validateSessionTime(session);

        return sessionRepository.save(session);
    }

    // ==========================================
    // GET SESSIONS BY BATCH
    // ==========================================

    @Override
    public List<BatchSession> getSessionsByBatchId(
            Long batchId) {

        // Check whether the batch exists
        batchRepository.findById(batchId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Batch not found with id: "
                                        + batchId
                        ));

        return sessionRepository.findByBatchId(batchId);
    }

    // ==========================================
    // GET SESSION BY ID
    // ==========================================

    @Override
    public BatchSession getSessionById(Long id) {

        return sessionRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Session not found with id: "
                                        + id
                        ));
    }

    // ==========================================
    // UPDATE SESSION
    // ==========================================

    @Override
    public BatchSession updateSession(
            Long id,
            BatchSession session) {

        BatchSession existing =
                getSessionById(id);

        // Check whether the new batch exists
        batchRepository.findById(
                session.getBatchId()
        ).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Batch not found with id: "
                                + session.getBatchId()
                ));

        // Validate session time
        validateSessionTime(session);

        existing.setBatchId(
                session.getBatchId());

        existing.setSessionDate(
                session.getSessionDate());

        existing.setStartTime(
                session.getStartTime());

        existing.setEndTime(
                session.getEndTime());

        existing.setTopic(
                session.getTopic());

        existing.setStatus(
                session.getStatus());

        return sessionRepository.save(existing);
    }

    // ==========================================
    // DELETE SESSION
    // ==========================================

    @Override
    public void deleteSession(Long id) {

        BatchSession existing =
                getSessionById(id);

        sessionRepository.delete(existing);
    }

    // ==========================================
    // SESSION TIME VALIDATION
    // ==========================================

    private void validateSessionTime(
            BatchSession session) {

        if (session.getStartTime() != null
                && session.getEndTime() != null
                && !session.getEndTime()
                        .isAfter(session.getStartTime())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End time must be after start time"
            );
        }
    }
}