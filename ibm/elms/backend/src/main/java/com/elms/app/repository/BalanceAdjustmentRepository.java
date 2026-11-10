package com.elms.app.repository;

import com.elms.app.entity.BalanceAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceAdjustmentRepository extends JpaRepository<BalanceAdjustment, Long> {
    List<BalanceAdjustment> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
}
