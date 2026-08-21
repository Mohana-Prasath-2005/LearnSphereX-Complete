package com.learnspherex.batch.service;

import java.util.List;

import com.learnspherex.batch.entity.Holiday;

public interface HolidayService {

    Holiday createHoliday(Holiday holiday);

    List<Holiday> getAllHolidays();

    // Get only holidays specifically assigned to a batch
    List<Holiday> getHolidaysByBatchId(Long batchId);

    // Get all holidays applicable to a batch:
    // Government + Institute + Batch-specific
    List<Holiday> getApplicableHolidaysByBatchId(Long batchId);

    Holiday getHolidayById(Long id);

    Holiday updateHoliday(Long id, Holiday holiday);

    void deleteHoliday(Long id);
}