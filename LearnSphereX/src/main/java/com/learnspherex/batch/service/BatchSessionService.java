package com.learnspherex.batch.service;

import java.util.List;

import com.learnspherex.batch.entity.BatchSession;

public interface BatchSessionService {

    BatchSession createSession(BatchSession session);

    List<BatchSession> getSessionsByBatchId(Long batchId);

    BatchSession getSessionById(Long id);

    BatchSession updateSession(Long id, BatchSession session);

    void deleteSession(Long id);
}