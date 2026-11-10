package com.ibm.elms.controller;

import com.ibm.elms.dto.LeaveDtos.BalanceAdjustmentRequest;
import com.ibm.elms.dto.LeaveDtos.LeaveBalanceResponse;
import com.ibm.elms.security.SecurityUtils;
import com.ibm.elms.service.LeaveBalanceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-balances")
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    public LeaveBalanceController(LeaveBalanceService leaveBalanceService) {
        this.leaveBalanceService = leaveBalanceService;
    }

    @GetMapping("/me")
    public List<LeaveBalanceResponse> myBalances(@RequestParam(required = false) Integer year) {
        return leaveBalanceService.forEmployee(SecurityUtils.currentUser().getId(), year);
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN','MANAGER')")
    public List<LeaveBalanceResponse> employeeBalances(
            @PathVariable Long employeeId, @RequestParam(required = false) Integer year) {
        return leaveBalanceService.forEmployee(employeeId, year);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN')")
    public List<LeaveBalanceResponse> all(@RequestParam(required = false) Integer year) {
        return leaveBalanceService.all(year);
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN')")
    public LeaveBalanceResponse adjust(@Valid @RequestBody BalanceAdjustmentRequest request) {
        return leaveBalanceService.adjust(request);
    }
}
