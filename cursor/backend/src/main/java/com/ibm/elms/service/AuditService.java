package com.ibm.elms.service;

import com.ibm.elms.domain.entity.AuditLog;
import com.ibm.elms.domain.entity.UserAccount;
import com.ibm.elms.repository.AuditLogRepository;
import com.ibm.elms.repository.UserAccountRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserAccountRepository userAccountRepository;

    public AuditService(AuditLogRepository auditLogRepository, UserAccountRepository userAccountRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public void log(Long actorId, String action, String entityType, Long entityId, String details) {
        AuditLog log = new AuditLog();
        if (actorId != null) {
            UserAccount actor = userAccountRepository.findById(actorId).orElse(null);
            log.setActor(actor);
        }
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list() {
        return auditLogRepository.findAllDetailed().stream()
                .map(a -> Map.<String, Object>of(
                        "id", a.getId(),
                        "action", a.getAction(),
                        "entityType", a.getEntityType(),
                        "entityId", a.getEntityId() == null ? 0 : a.getEntityId(),
                        "actorName", a.getActor() == null ? "system" : a.getActor().getFullName(),
                        "details", a.getDetails() == null ? "" : a.getDetails(),
                        "createdAt", a.getCreatedAt().toString()))
                .toList();
    }
}
