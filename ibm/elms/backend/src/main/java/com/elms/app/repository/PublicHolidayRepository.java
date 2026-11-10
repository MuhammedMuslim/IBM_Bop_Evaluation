package com.elms.app.repository;

import com.elms.app.entity.PublicHoliday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, Long> {
    List<PublicHoliday> findByHolidayDateBetween(LocalDate start, LocalDate end);
    boolean existsByHolidayDate(LocalDate date);
}
