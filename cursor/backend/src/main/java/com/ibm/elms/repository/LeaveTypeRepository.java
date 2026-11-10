package com.ibm.elms.repository;

import com.ibm.elms.domain.entity.LeaveType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    Optional<LeaveType> findByNameIgnoreCase(String name);

    List<LeaveType> findByActiveTrue();

    boolean existsByNameIgnoreCase(String name);
}
