package com.ibm.elms.controller;

import com.ibm.elms.dto.LeaveDtos.CreateLeaveRequest;
import com.ibm.elms.dto.LeaveDtos.DecisionRequest;
import com.ibm.elms.dto.LeaveDtos.LeaveRequestResponse;
import com.ibm.elms.service.LeaveRequestService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping
    public LeaveRequestResponse submit(@Valid @RequestBody CreateLeaveRequest request) {
        return leaveRequestService.submit(request);
    }

    @GetMapping("/me")
    public List<LeaveRequestResponse> myRequests() {
        return leaveRequestService.myRequests();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER')")
    public List<LeaveRequestResponse> pending() {
        return leaveRequestService.pendingForManager();
    }

    @GetMapping("/team")
    @PreAuthorize("hasRole('MANAGER')")
    public List<LeaveRequestResponse> teamHistory() {
        return leaveRequestService.teamHistory();
    }

    @GetMapping("/team/schedule")
    @PreAuthorize("hasRole('MANAGER')")
    public List<LeaveRequestResponse> teamSchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return leaveRequestService.teamSchedule(start, end);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN')")
    public List<LeaveRequestResponse> all() {
        return leaveRequestService.all();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','HR_ADMIN','SYSTEM_ADMIN')")
    public LeaveRequestResponse approve(@PathVariable Long id, @RequestBody(required = false) DecisionRequest request) {
        return leaveRequestService.decide(id, true, request);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER','HR_ADMIN','SYSTEM_ADMIN')")
    public LeaveRequestResponse reject(@PathVariable Long id, @RequestBody(required = false) DecisionRequest request) {
        return leaveRequestService.decide(id, false, request);
    }

    @PostMapping("/{id}/cancel")
    public LeaveRequestResponse cancel(@PathVariable Long id) {
        return leaveRequestService.cancel(id);
    }
}
