package com.learnspherex.batch.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.learnspherex.batch.entity.Batch;
import com.learnspherex.batch.entity.Holiday;
import com.learnspherex.batch.entity.HolidayType;
import com.learnspherex.batch.repository.BatchRepository;
import com.learnspherex.batch.repository.HolidayRepository;

@Service
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;
    private final BatchRepository batchRepository;

    public HolidayServiceImpl(
            HolidayRepository holidayRepository,
            BatchRepository batchRepository) {

        this.holidayRepository = holidayRepository;
        this.batchRepository = batchRepository;
    }

    // ==========================================
    // CREATE HOLIDAY
    // ==========================================

    @Override
    public Holiday createHoliday(Holiday holiday) {

        validateHoliday(holiday);

        /*
         * BATCH holiday
         * ----------------
         * Must have a valid batch.
         */
        if (holiday.getHolidayType() == HolidayType.BATCH) {

            validateBatch(holiday.getBatchId());

            boolean exists =
                    holidayRepository
                            .existsByBatchIdAndHolidayDateAndHolidayType(
                                    holiday.getBatchId(),
                                    holiday.getHolidayDate(),
                                    holiday.getHolidayType());

            if (exists) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Holiday already exists for this batch on "
                                + holiday.getHolidayDate());
            }

        } else {

            /*
             * GOVERNMENT / INSTITUTE holiday
             * --------------------------------
             * These are global holidays.
             * They should not belong to one batch.
             */
            holiday.setBatchId(null);

            boolean exists =
                    holidayRepository
                            .existsByHolidayDateAndHolidayType(
                                    holiday.getHolidayDate(),
                                    holiday.getHolidayType());

            if (exists) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        holiday.getHolidayType()
                                + " holiday already exists on "
                                + holiday.getHolidayDate());
            }
        }

        return holidayRepository.save(holiday);
    }

    // ==========================================
    // GET ALL HOLIDAYS
    // ==========================================

    @Override
    public List<Holiday> getAllHolidays() {

        return holidayRepository.findAll();
    }

    // ==========================================
    // GET BATCH-SPECIFIC HOLIDAYS
    // ==========================================

    @Override
    public List<Holiday> getHolidaysByBatchId(
            Long batchId) {

        validateBatch(batchId);

        return holidayRepository.findByBatchId(batchId);
    }

    // ==========================================
    // GET ALL APPLICABLE HOLIDAYS
    // ==========================================

    @Override
    public List<Holiday> getApplicableHolidaysByBatchId(
            Long batchId) {

        validateBatch(batchId);

        /*
         * Returns:
         *
         * 1. Batch-specific holidays
         * 2. Government holidays
         * 3. Institute holidays
         *
         * Government and institute holidays have
         * batchId = NULL.
         */
        return holidayRepository
                .findByBatchIdOrBatchIdIsNull(batchId);
    }

    // ==========================================
    // GET HOLIDAY BY ID
    // ==========================================

    @Override
    public Holiday getHolidayById(Long id) {

        return holidayRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Holiday not found with id: "
                                        + id));
    }

    // ==========================================
    // UPDATE HOLIDAY
    // ==========================================

    @Override
    public Holiday updateHoliday(
            Long id,
            Holiday holiday) {

        Holiday existingHoliday =
                getHolidayById(id);

        validateHoliday(holiday);

        /*
         * BATCH holiday
         */
        if (holiday.getHolidayType() == HolidayType.BATCH) {

            validateBatch(holiday.getBatchId());

            boolean exists =
                    holidayRepository
                            .existsByBatchIdAndHolidayDateAndHolidayTypeAndIdNot(
                                    holiday.getBatchId(),
                                    holiday.getHolidayDate(),
                                    holiday.getHolidayType(),
                                    id);

            if (exists) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Holiday already exists for this batch on "
                                + holiday.getHolidayDate());
            }

        } else {

            /*
             * GOVERNMENT / INSTITUTE holiday
             */
            holiday.setBatchId(null);

            boolean exists =
                    holidayRepository
                            .existsByHolidayDateAndHolidayTypeAndIdNot(
                                    holiday.getHolidayDate(),
                                    holiday.getHolidayType(),
                                    id);

            if (exists) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        holiday.getHolidayType()
                                + " holiday already exists on "
                                + holiday.getHolidayDate());
            }
        }

        existingHoliday.setBatchId(
                holiday.getBatchId());

        existingHoliday.setHolidayDate(
                holiday.getHolidayDate());

        existingHoliday.setReason(
                holiday.getReason());

        existingHoliday.setHolidayType(
                holiday.getHolidayType());

        return holidayRepository.save(
                existingHoliday);
    }

    // ==========================================
    // DELETE HOLIDAY
    // ==========================================

    @Override
    public void deleteHoliday(Long id) {

        Holiday existingHoliday =
                getHolidayById(id);

        holidayRepository.delete(existingHoliday);
    }

    // ==========================================
    // VALIDATE HOLIDAY
    // ==========================================

    private void validateHoliday(
            Holiday holiday) {

        if (holiday == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Holiday data is required");
        }

        if (holiday.getHolidayDate() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Holiday date is required");
        }

        if (holiday.getReason() == null
                || holiday.getReason().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Holiday reason is required");
        }

        if (holiday.getHolidayType() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Holiday type is required");
        }

        /*
         * A BATCH holiday must have a batch ID.
         */
        if (holiday.getHolidayType()
                == HolidayType.BATCH
                && holiday.getBatchId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Batch ID is required for batch holiday");
        }
    }

    // ==========================================
    // VALIDATE BATCH
    // ==========================================

    private void validateBatch(Long batchId) {

        if (batchId == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Batch ID is required");
        }

        Batch batch = batchRepository
                .findById(batchId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Batch not found with id: "
                                        + batchId));
    }
}