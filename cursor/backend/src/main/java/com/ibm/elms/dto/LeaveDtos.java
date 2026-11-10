package com.ibm.elms.dto;

import com.ibm.elms.domain.enums.LeaveRequestStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class LeaveDtos {

    private LeaveDtos() {}

    public record LeaveTypeRequest(
            @NotBlank String name,
            String description,
            boolean paid,
            boolean requiresApproval,
            boolean active) {}

    public record LeaveTypeResponse(
            Long id,
            String name,
            String description,
            boolean paid,
            boolean requiresApproval,
            boolean active) {}

    public record LeavePolicyRequest(
            @NotBlank String name,
            @NotNull Long leaveTypeId,
            int annualEntitlementDays,
            boolean excludeWeekends,
            boolean excludePublicHolidays,
            boolean allowNegativeBalance,
            boolean restoreBalanceOnCancel,
            int maxConsecutiveDays,
            boolean active) {}

    public record LeavePolicyResponse(
            Long id,
            String name,
            Long leaveTypeId,
            String leaveTypeName,
            int annualEntitlementDays,
            boolean excludeWeekends,
            boolean excludePublicHolidays,
            boolean allowNegativeBalance,
            boolean restoreBalanceOnCancel,
            int maxConsecutiveDays,
            boolean active) {}

    public record LeaveBalanceResponse(
            Long id,
            Long employeeId,
            String employeeName,
            Long leaveTypeId,
            String leaveTypeName,
            int year,
            BigDecimal entitled,
            BigDecimal used,
            BigDecimal pending,
            BigDecimal adjustment,
            BigDecimal available) {}

    public record BalanceAdjustmentRequest(
            @NotNull Long employeeId,
            @NotNull Long leaveTypeId,
            @NotNull Integer year,
            @NotNull @DecimalMin("0.0") BigDecimal entitled,
            @NotNull BigDecimal adjustment,
            @NotBlank String reason) {}

    public record CreateLeaveRequest(
            @NotNull Long leaveTypeId,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            String reason) {}

    public record DecisionRequest(String comment) {}

    public record LeaveRequestResponse(
            Long id,
            Long employeeId,
            String employeeName,
            Long leaveTypeId,
            String leaveTypeName,
            Long managerId,
            String managerName,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal durationDays,
            String reason,
            LeaveRequestStatus status,
            String managerComment,
            Instant decidedAt,
            Instant createdAt) {}

    public record ReportSummary(
            long totalEmployees,
            long pendingRequests,
            long approvedRequests,
            long rejectedRequests,
            long cancelledRequests) {}
}
