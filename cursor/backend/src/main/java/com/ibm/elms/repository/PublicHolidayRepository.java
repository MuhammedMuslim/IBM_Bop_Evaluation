package com.ibm.elms.repository;

import com.ibm.elms.domain.entity.PublicHoliday;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, Long> {

    List<PublicHoliday> findByDateBetween(LocalDate start, LocalDate end);
}
