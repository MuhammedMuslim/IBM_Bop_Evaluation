package com.elms.app.controller;

import com.elms.app.dto.*;
import com.elms.app.entity.*;
import com.elms.app.repository.*;
import com.elms.app.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
public class HrController {

    private final UserService         userService;
    private final LeaveRequestService leaveRequestService;
    private final LeaveBalanceService leaveBalanceService;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final AuditLogRepository  auditLogRepository;

    // ── Employees ──────────────────────────────────────────────────────────

    @GetMapping("/employees")
    public ResponseEntity<List<UserDto>> getAllEmployees() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<UserDto> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/employees")
    public ResponseEntity<UserDto> createEmployee(@Valid @RequestBody CreateUserRequest req,
                                                  Authentication auth) {
        User actor = userService.findByEmail(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(req, actor));
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<UserDto> updateEmployee(@PathVariable Long id,
                                                   @RequestBody CreateUserRequest req,
                                                   Authentication auth) {
        User actor = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(userService.updateUser(id, req, actor));
    }

    @PatchMapping("/employees/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id, Authentication auth) {
        User actor = userService.findByEmail(auth.getName());
        userService.deactivateUser(id, actor);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/employees/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id, Authentication auth) {
        User actor = userService.findByEmail(auth.getName());
        userService.activateUser(id, actor);
        return ResponseEntity.noContent().build();
    }

    // ── Leave Types ─────────────────────────────────────────────────────────

    @GetMapping("/leave-types")
    public ResponseEntity<List<LeaveType>> getLeaveTypes() {
        return ResponseEntity.ok(leaveTypeRepository.findAll());
    }

    @PostMapping("/leave-types")
    public ResponseEntity<LeaveType> createLeaveType(@RequestBody LeaveType lt) {
        lt.setId(null);
        LeaveType saved = leaveTypeRepository.save(lt);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/leave-types/{id}")
    public ResponseEntity<LeaveType> updateLeaveType(@PathVariable Long id,
                                                     @RequestBody LeaveType lt) {
        lt.setId(id);
        return ResponseEntity.ok(leaveTypeRepository.save(lt));
    }

    // ── Leave Policies ──────────────────────────────────────────────────────

    @GetMapping("/policies")
    public ResponseEntity<List<LeavePolicy>> getPolicies() {
        return ResponseEntity.ok(leavePolicyRepository.findAll());
    }

    @PostMapping("/policies")
    public ResponseEntity<LeavePolicy> createPolicy(@RequestBody LeavePolicy policy) {
        policy.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(leavePolicyRepository.save(policy));
    }

    @PutMapping("/policies/{id}")
    public ResponseEntity<LeavePolicy> updatePolicy(@PathVariable Long id,
                                                    @RequestBody LeavePolicy policy) {
        policy.setId(id);
        return ResponseEntity.ok(leavePolicyRepository.save(policy));
    }

    // ── Leave Balances ──────────────────────────────────────────────────────

    @GetMapping("/employees/{employeeId}/balances")
    public ResponseEntity<List<LeaveBalanceDto>> getBalances(@PathVariable Long employeeId,
                                                              @RequestParam(defaultValue = "0") int year) {
        int y = year == 0 ? java.time.LocalDate.now().getYear() : year;
        return ResponseEntity.ok(leaveBalanceService.getBalancesForEmployee(employeeId, y));
    }

    @PostMapping("/balances/adjust")
    public ResponseEntity<LeaveBalanceDto> adjustBalance(@Valid @RequestBody AdjustBalanceRequest req,
                                                          Authentication auth) {
        User actor = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(leaveBalanceService.adjust(req, actor));
    }

    @PostMapping("/balances/bulk-assign")
    public ResponseEntity<Void> bulkAssign(@RequestParam int year, Authentication auth) {
        User actor = userService.findByEmail(auth.getName());
        leaveBalanceService.bulkAssignEntitlements(year, actor);
        return ResponseEntity.ok().build();
    }

    // ── All Leave Requests ──────────────────────────────────────────────────

    @GetMapping("/leave-requests")
    public ResponseEntity<List<LeaveRequestResponse>> allRequests() {
        return ResponseEntity.ok(leaveRequestService.getAll());
    }

    // ── Audit Logs ──────────────────────────────────────────────────────────

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> auditLogs(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(page, size)));
    }
}
