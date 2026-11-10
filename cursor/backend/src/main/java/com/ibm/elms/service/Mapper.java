package com.ibm.elms.service;

import com.ibm.elms.domain.entity.*;
import com.ibm.elms.dto.LeaveDtos.*;
import com.ibm.elms.dto.UserDtos.UserResponse;

public final class Mapper {

    private Mapper() {}

    public static UserResponse toUser(UserAccount user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmployeeCode(),
                user.getDepartment(),
                user.getJobTitle(),
                user.getRole(),
                user.getStatus(),
                user.getManager() == null ? null : user.getManager().getId(),
                user.getManager() == null ? null : user.getManager().getFullName());
    }

    public static LeaveTypeResponse toLeaveType(LeaveType type) {
        return new LeaveTypeResponse(
                type.getId(),
                type.getName(),
                type.getDescription(),
                type.isPaid(),
                type.isRequiresApproval(),
                type.isActive());
    }

    public static LeavePolicyResponse toPolicy(LeavePolicy policy) {
        return new LeavePolicyResponse(
                policy.getId(),
                policy.getName(),
                policy.getLeaveType().getId(),
                policy.getLeaveType().getName(),
                policy.getAnnualEntitlementDays(),
                policy.isExcludeWeekends(),
                policy.isExcludePublicHolidays(),
                policy.isAllowNegativeBalance(),
                policy.isRestoreBalanceOnCancel(),
                policy.getMaxConsecutiveDays(),
                policy.isActive());
    }

    public static LeaveBalanceResponse toBalance(LeaveBalance balance) {
        return new LeaveBalanceResponse(
                balance.getId(),
                balance.getEmployee().getId(),
                balance.getEmployee().getFullName(),
                balance.getLeaveType().getId(),
                balance.getLeaveType().getName(),
                balance.getYear(),
                balance.getEntitled(),
                balance.getUsed(),
                balance.getPending(),
                balance.getAdjustment(),
                balance.getAvailable());
    }

    public static LeaveRequestResponse toRequest(LeaveRequest request) {
        return new LeaveRequestResponse(
                request.getId(),
                request.getEmployee().getId(),
                request.getEmployee().getFullName(),
                request.getLeaveType().getId(),
                request.getLeaveType().getName(),
                request.getManager() == null ? null : request.getManager().getId(),
                request.getManager() == null ? null : request.getManager().getFullName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getDurationDays(),
                request.getReason(),
                request.getStatus(),
                request.getManagerComment(),
                request.getDecidedAt(),
                request.getCreatedAt());
    }
}
