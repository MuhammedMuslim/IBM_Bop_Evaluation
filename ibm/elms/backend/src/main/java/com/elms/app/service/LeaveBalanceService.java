package com.elms.app.service;

import com.elms.app.dto.*;
import com.elms.app.entity.*;
import com.elms.app.exception.*;
import com.elms.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveBalanceService {

    private final LeaveBalanceRepository      leaveBalanceRepository;
    private final LeaveTypeRepository         leaveTypeRepository;
    private final LeavePolicyRepository       leavePolicyRepository;
    private final UserService                 userService;
    private final AuditService                auditService;
    private final BalanceAdjustmentRepository balanceAdjustmentRepository;

    public List<LeaveBalanceDto> getBalancesForEmployee(Long employeeId, int year) {
        return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year)
                .stream().map(this::toDto).toList();
    }

    public List<LeaveBalanceDto> getAllBalancesForEmployee(Long employeeId) {
        return leaveBalanceRepository.findByEmployeeId(employeeId)
                .stream().map(this::toDto).toList();
    }

    /**
     * Bulk-assign entitlements for all active employees for a given year.
     * Called by HR at year start.
     */
    @Transactional
    public void bulkAssignEntitlements(int year, User actor) {
        List<User> employees = userService.getAllUsers().stream()
                .filter(u -> u.isActive() &&
                    (u.getRole().name().equals("EMPLOYEE") || u.getRole().name().equals("MANAGER")))
                .map(u -> userService.findById(u.getId()))
                .toList();

        List<LeaveType> types = leaveTypeRepository.findByActiveTrue();

        for (User emp : employees) {
            for (LeaveType lt : types) {
                leavePolicyRepository.findByLeaveTypeIdAndActiveTrue(lt.getId()).ifPresent(policy -> {
                    boolean exists = leaveBalanceRepository
                            .findByEmployeeIdAndLeaveTypeIdAndYear(emp.getId(), lt.getId(), year)
                            .isPresent();
                    if (!exists) {
                        LeaveBalance bal = LeaveBalance.builder()
                                .employee(emp).leaveType(lt).year(year)
                                .totalDays(BigDecimal.valueOf(policy.getAnnualEntitlementDays()))
                                .usedDays(BigDecimal.ZERO).pendingDays(BigDecimal.ZERO)
                                .build();
                        leaveBalanceRepository.save(bal);
                    }
                });
            }
        }
        auditService.log(actor, "BULK_ASSIGN_ENTITLEMENTS", "LeaveBalance", null,
                "Bulk assigned entitlements for year " + year);
    }

    @Transactional
    public LeaveBalanceDto adjust(AdjustBalanceRequest req, User actor) {
        User employee = userService.findById(req.getEmployeeId());
        LeaveType leaveType = leaveTypeRepository.findById(req.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(req.getEmployeeId(), req.getLeaveTypeId(), req.getYear())
                .orElseGet(() -> {
                    LeaveBalance nb = LeaveBalance.builder()
                            .employee(employee)
                            .leaveType(leaveType)
                            .year(req.getYear())
                            .totalDays(BigDecimal.ZERO)
                            .usedDays(BigDecimal.ZERO)
                            .pendingDays(BigDecimal.ZERO)
                            .build();
                    return leaveBalanceRepository.save(nb);
                });

        balance.setTotalDays(balance.getTotalDays().add(req.getDeltaDays()));
        balance = leaveBalanceRepository.save(balance);

        BalanceAdjustment adj = BalanceAdjustment.builder()
                .employee(employee)
                .leaveType(leaveType)
                .year(req.getYear())
                .adjustedBy(actor)
                .deltaDays(req.getDeltaDays())
                .reason(req.getReason())
                .build();
        balanceAdjustmentRepository.save(adj);

        auditService.log(actor, "ADJUST_BALANCE", "LeaveBalance", balance.getId(),
                "Adjusted " + leaveType.getName() + " by " + req.getDeltaDays() + " — " + req.getReason());

        return toDto(balance);
    }

    public LeaveBalanceDto toDto(LeaveBalance b) {
        return LeaveBalanceDto.builder()
                .id(b.getId())
                .leaveTypeId(b.getLeaveType().getId())
                .leaveTypeName(b.getLeaveType().getName())
                .year(b.getYear())
                .totalDays(b.getTotalDays())
                .usedDays(b.getUsedDays())
                .pendingDays(b.getPendingDays())
                .availableDays(b.getAvailableDays())
                .build();
    }
}
