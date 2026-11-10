package com.ibm.elms.repository;

import com.ibm.elms.domain.entity.LeaveRequest;
import com.ibm.elms.domain.enums.LeaveRequestStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @Query("""
            select r from LeaveRequest r
            join fetch r.employee
            join fetch r.leaveType
            left join fetch r.manager
            where r.id = :id
            """)
    Optional<LeaveRequest> findDetailedById(@Param("id") Long id);

    @Query("""
            select r from LeaveRequest r
            join fetch r.employee
            join fetch r.leaveType
            left join fetch r.manager
            where r.employee.id = :employeeId
            order by r.createdAt desc
            """)
    List<LeaveRequest> findByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("""
            select r from LeaveRequest r
            join fetch r.employee
            join fetch r.leaveType
            left join fetch r.manager
            where r.manager.id = :managerId and r.status = :status
            order by r.createdAt asc
            """)
    List<LeaveRequest> findByManagerIdAndStatus(
            @Param("managerId") Long managerId, @Param("status") LeaveRequestStatus status);

    @Query("""
            select r from LeaveRequest r
            join fetch r.employee
            join fetch r.leaveType
            left join fetch r.manager
            where r.manager.id = :managerId
            order by r.createdAt desc
            """)
    List<LeaveRequest> findByManagerId(@Param("managerId") Long managerId);

    @Query("""
            select r from LeaveRequest r
            join fetch r.employee
            join fetch r.leaveType
            left join fetch r.manager
            order by r.createdAt desc
            """)
    List<LeaveRequest> findAllDetailed();

    @Query("""
            select count(r) > 0 from LeaveRequest r
            where r.employee.id = :employeeId
              and r.status in :statuses
              and r.startDate <= :endDate
              and r.endDate >= :startDate
              and (:excludeId is null or r.id <> :excludeId)
            """)
    boolean existsOverlapping(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<LeaveRequestStatus> statuses,
            @Param("excludeId") Long excludeId);

    @Query("""
            select r from LeaveRequest r
            join fetch r.employee e
            join fetch r.leaveType
            where e.manager.id = :managerId
              and r.status = com.ibm.elms.domain.enums.LeaveRequestStatus.APPROVED
              and r.startDate <= :endDate
              and r.endDate >= :startDate
            order by r.startDate
            """)
    List<LeaveRequest> findTeamApprovedInRange(
            @Param("managerId") Long managerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    long countByStatus(LeaveRequestStatus status);
}
