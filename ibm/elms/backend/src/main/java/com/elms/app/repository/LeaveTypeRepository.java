package com.elms.app.repository;

import com.elms.app.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    List<LeaveType> findByActiveTrue();
    boolean existsByNameIgnoreCase(String name);
}
