package com.ibm.elms.repository;

import com.ibm.elms.domain.entity.LeavePolicy;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long> {

    List<LeavePolicy> findByActiveTrue();

    @Query("select p from LeavePolicy p join fetch p.leaveType where p.leaveType.id = :leaveTypeId and p.active = true")
    Optional<LeavePolicy> findActiveByLeaveTypeId(@Param("leaveTypeId") Long leaveTypeId);

    boolean existsByNameIgnoreCase(String name);
}
