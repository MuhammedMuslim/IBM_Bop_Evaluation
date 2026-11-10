package com.elms.app.repository;

import com.elms.app.entity.LeavePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long> {
    List<LeavePolicy> findByActiveTrue();
    Optional<LeavePolicy> findByLeaveTypeIdAndActiveTrue(Long leaveTypeId);
}
