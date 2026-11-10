package com.elms.app.service;

import com.elms.app.entity.*;
import com.elms.app.exception.ResourceNotFoundException;
import com.elms.app.repository.PublicHolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkingDaysCalculator {

    private final PublicHolidayRepository publicHolidayRepository;

    /**
     * Counts working days between startDate and endDate (inclusive),
     * optionally excluding weekends and public holidays.
     */
    public java.math.BigDecimal calculate(LocalDate startDate, LocalDate endDate,
                                           boolean excludeWeekends,
                                           boolean excludePublicHolidays) {
        Set<LocalDate> holidays = Set.of();
        if (excludePublicHolidays) {
            holidays = publicHolidayRepository
                    .findByHolidayDateBetween(startDate, endDate)
                    .stream()
                    .map(PublicHoliday::getHolidayDate)
                    .collect(Collectors.toSet());
        }

        long count = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            boolean isWeekend = excludeWeekends &&
                    (current.getDayOfWeek() == DayOfWeek.SATURDAY ||
                     current.getDayOfWeek() == DayOfWeek.SUNDAY);
            boolean isHoliday = excludePublicHolidays && holidays.contains(current);
            if (!isWeekend && !isHoliday) {
                count++;
            }
            current = current.plusDays(1);
        }
        return java.math.BigDecimal.valueOf(count);
    }
}
