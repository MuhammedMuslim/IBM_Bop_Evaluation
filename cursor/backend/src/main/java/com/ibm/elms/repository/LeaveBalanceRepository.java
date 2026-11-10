package com.ibm.elms.repository;

import com.ibm.elms.domain.entity.LeaveBalance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    @Query("select b from LeaveBalance b join fetch b.leaveType where b.employee.id = :employeeId and b.year = :year")
    List<LeaveBalance> findByEmployeeIdAndYear(@Param("employeeId") Long employeeId, @Param("year") int year);

    @Query("select b from LeaveBalance b join fetch b.leaveType join fetch b.employee where b.employee.id = :employeeId and b.leaveType.id = :leaveTypeId and b.year = :year")
    Optional<LeaveBalance> findByEmployeeAndTypeAndYear(
            @Param("employeeId") Long employeeId,
            @Param("leaveTypeId") Long leaveTypeId,
            @Param("year") int year);

    List<LeaveBalance> findByYear(int year);
}
