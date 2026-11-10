package com.elms.app.controller;

import com.elms.app.dto.*;
import com.elms.app.entity.User;
import com.elms.app.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final LeaveRequestService leaveRequestService;
    private final UserService userService;
    private final LeaveBalanceService leaveBalanceService;

    /** Manager sees pending requests from their team */
    @GetMapping("/requests/pending")
    public ResponseEntity<List<LeaveRequestResponse>> pending(Authentication auth) {
        User manager = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(leaveRequestService.getPendingForManager(manager));
    }

    /** Manager sees all team requests (history) */
    @GetMapping("/requests")
    public ResponseEntity<List<LeaveRequestResponse>> all(Authentication auth) {
        User manager = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(leaveRequestService.getAllForManager(manager));
    }

    /** Manager approves or rejects a request */
    @PatchMapping("/requests/{id}/review")
    public ResponseEntity<LeaveRequestResponse> review(@PathVariable Long id,
                                                       @Valid @RequestBody ReviewRequest dto,
                                                       Authentication auth) {
        User manager = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(leaveRequestService.review(id, dto, manager));
    }

    /** Manager views their team members */
    @GetMapping("/team")
    public ResponseEntity<List<UserDto>> team(Authentication auth) {
        User manager = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(userService.getTeamByManager(manager.getId()));
    }

    /** Manager views balances for a specific employee */
    @GetMapping("/employees/{employeeId}/balances")
    public ResponseEntity<List<LeaveBalanceDto>> employeeBalances(@PathVariable Long employeeId,
                                                                   @RequestParam(defaultValue = "0") int year,
                                                                   Authentication auth) {
        int y = year == 0 ? java.time.LocalDate.now().getYear() : year;
        return ResponseEntity.ok(leaveBalanceService.getBalancesForEmployee(employeeId, y));
    }
}
