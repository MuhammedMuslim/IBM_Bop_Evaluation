package com.ibm.elms.repository;

import com.ibm.elms.domain.entity.AuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
            select a from AuditLog a
            left join fetch a.actor
            order by a.createdAt desc
            """)
    List<AuditLog> findAllDetailed();
}
