package com.ibm.elms.service;

import com.ibm.elms.domain.entity.LeaveBalance;
import com.ibm.elms.domain.entity.LeaveType;
import com.ibm.elms.domain.entity.UserAccount;
import com.ibm.elms.dto.LeaveDtos.BalanceAdjustmentRequest;
import com.ibm.elms.dto.LeaveDtos.LeaveBalanceResponse;
import com.ibm.elms.exception.ApiException;
import com.ibm.elms.repository.LeaveBalanceRepository;
import com.ibm.elms.repository.LeaveTypeRepository;
import com.ibm.elms.repository.UserAccountRepository;
import com.ibm.elms.security.SecurityUtils;
import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserAccountRepository userAccountRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final AuditService auditService;

    public LeaveBalanceService(
            LeaveBalanceRepository leaveBalanceRepository,
            UserAccountRepository userAccountRepository,
            LeaveTypeRepository leaveTypeRepository,
            AuditService auditService) {
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.userAccountRepository = userAccountRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> forEmployee(Long employeeId, Integer year) {
        int y = year == null ? Year.now().getValue() : year;
        return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, y).stream()
                .map(Mapper::toBalance)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> all(Integer year) {
        int y = year == null ? Year.now().getValue() : year;
        return leaveBalanceRepository.findByYear(y).stream().map(Mapper::toBalance).toList();
    }

    @Transactional
    public LeaveBalanceResponse adjust(BalanceAdjustmentRequest request) {
        if (request.reason() == null || request.reason().isBlank()) {
            throw new ApiException("Adjustment reason is required", HttpStatus.BAD_REQUEST);
        }
        UserAccount employee = userAccountRepository
                .findById(request.employeeId())
                .orElseThrow(() -> new ApiException("Employee not found", HttpStatus.NOT_FOUND));
        LeaveType leaveType = leaveTypeRepository
                .findById(request.leaveTypeId())
                .orElseThrow(() -> new ApiException("Leave type not found", HttpStatus.NOT_FOUND));

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeAndTypeAndYear(request.employeeId(), request.leaveTypeId(), request.year())
                .orElseGet(() -> {
                    LeaveBalance created = new LeaveBalance();
                    created.setEmployee(employee);
                    created.setLeaveType(leaveType);
                    created.setYear(request.year());
                    created.setEntitled(BigDecimal.ZERO);
                    return created;
                });

        balance.setEntitled(request.entitled());
        balance.setAdjustment(request.adjustment());
        leaveBalanceRepository.save(balance);

        auditService.log(
                SecurityUtils.currentUser().getId(),
                "ADJUST_BALANCE",
                "LeaveBalance",
                balance.getId(),
                request.reason());
        return Mapper.toBalance(balance);
    }

    @Transactional
    public LeaveBalance requireBalance(Long employeeId, Long leaveTypeId, int year) {
        return leaveBalanceRepository
                .findByEmployeeAndTypeAndYear(employeeId, leaveTypeId, year)
                .orElseThrow(() -> new ApiException(
                        "No leave balance configured for this leave type/year", HttpStatus.BAD_REQUEST));
    }
}
