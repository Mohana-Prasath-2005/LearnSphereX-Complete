package com.learnspherex.batch.service;

import java.util.List;

import com.learnspherex.batch.entity.BatchSchedule;

public interface BatchScheduleService {

    BatchSchedule createSchedule(BatchSchedule schedule);

    List<BatchSchedule> getSchedulesByBatchId(Long batchId);

    List<BatchSchedule> getAllSchedules();

    BatchSchedule getScheduleById(Long id);

    BatchSchedule updateSchedule(Long id, BatchSchedule schedule);

    void deleteSchedule(Long id);
}