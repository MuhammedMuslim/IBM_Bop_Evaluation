package com.ibm.elms.service;

import com.ibm.elms.domain.entity.LeavePolicy;
import com.ibm.elms.domain.entity.PublicHoliday;
import com.ibm.elms.repository.PublicHolidayRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LeaveDurationService {

    private final PublicHolidayRepository publicHolidayRepository;

    public LeaveDurationService(PublicHolidayRepository publicHolidayRepository) {
        this.publicHolidayRepository = publicHolidayRepository;
    }

    public BigDecimal calculateWorkingDays(LocalDate start, LocalDate end, LeavePolicy policy) {
        Set<LocalDate> holidays = Set.of();
        if (policy.isExcludePublicHolidays()) {
            holidays = publicHolidayRepository.findByDateBetween(start, end).stream()
                    .map(PublicHoliday::getDate)
                    .collect(Collectors.toSet());
        }

        int days = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek dow = date.getDayOfWeek();
            boolean weekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
            if (policy.isExcludeWeekends() && weekend) {
                continue;
            }
            if (policy.isExcludePublicHolidays() && holidays.contains(date)) {
                continue;
            }
            days++;
        }
        return BigDecimal.valueOf(days);
    }
}
