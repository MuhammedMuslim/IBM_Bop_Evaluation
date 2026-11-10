package com.elms.app.repository;

import com.elms.app.entity.LeaveRequest;
import com.elms.app.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.employee.manager.id = :managerId
          AND lr.status = 'PENDING'
        ORDER BY lr.createdAt ASC
        """)
    List<LeaveRequest> findPendingByManagerId(@Param("managerId") Long managerId);

    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.employee.manager.id = :managerId
        ORDER BY lr.createdAt DESC
        """)
    List<LeaveRequest> findAllByManagerId(@Param("managerId") Long managerId);

    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.employee.id = :employeeId
          AND lr.status IN ('PENDING','APPROVED')
          AND (lr.startDate <= :endDate AND lr.endDate >= :startDate)
        """)
    List<LeaveRequest> findOverlapping(@Param("employeeId") Long employeeId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    List<LeaveRequest> findAllByOrderByCreatedAtDesc();

    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE (:status IS NULL OR lr.status = :status)
        ORDER BY lr.createdAt DESC
        """)
    List<LeaveRequest> findAllByStatusOptional(@Param("status") LeaveStatus status);
}
