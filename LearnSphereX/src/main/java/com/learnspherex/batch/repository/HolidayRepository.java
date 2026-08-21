package com.learnspherex.batch.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnspherex.batch.entity.Holiday;
import com.learnspherex.batch.entity.HolidayType;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    // Get holidays specifically assigned to a batch
    List<Holiday> findByBatchId(Long batchId);

    // Get batch-specific holidays + global holidays
    // where batchId is NULL
    List<Holiday> findByBatchIdOrBatchIdIsNull(Long batchId);

    // Get holidays by type
    List<Holiday> findByHolidayType(HolidayType holidayType);

    // Get holidays by date
    List<Holiday> findByHolidayDate(LocalDate holidayDate);

    // Check duplicate batch-specific holiday
    boolean existsByBatchIdAndHolidayDateAndHolidayType(
            Long batchId,
            LocalDate holidayDate,
            HolidayType holidayType);

    // Check duplicate government/institute holiday
    boolean existsByHolidayDateAndHolidayType(
            LocalDate holidayDate,
            HolidayType holidayType);

    // Check duplicate batch holiday while excluding
    // the current holiday during update
    boolean existsByBatchIdAndHolidayDateAndHolidayTypeAndIdNot(
            Long batchId,
            LocalDate holidayDate,
            HolidayType holidayType,
            Long id);

    // Check duplicate government/institute holiday
    // while excluding the current holiday during update
    boolean existsByHolidayDateAndHolidayTypeAndIdNot(
            LocalDate holidayDate,
            HolidayType holidayType,
            Long id);
}