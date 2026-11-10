package com.ibm.elms.controller;

import com.ibm.elms.dto.LeaveDtos.ReportSummary;
import com.ibm.elms.service.AuditService;
import com.ibm.elms.service.ReportService;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final AuditService auditService;

    public ReportController(ReportService reportService, AuditService auditService) {
        this.reportService = reportService;
        this.auditService = auditService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN')")
    public ReportSummary summary() {
        return reportService.summary();
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN')")
    public List<Map<String, Object>> auditLogs() {
        return auditService.list();
    }
}
