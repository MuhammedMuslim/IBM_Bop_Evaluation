package com.elms.app.controller;

import com.elms.app.dto.LeaveBalanceDto;
import com.elms.app.entity.User;
import com.elms.app.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final LeaveBalanceService leaveBalanceService;
    private final UserService userService;

    /** Employee views their own leave balances */
    @GetMapping("/balances")
    public ResponseEntity<List<LeaveBalanceDto>> myBalances(
            @RequestParam(defaultValue = "0") int year,
            Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        int y = year == 0 ? LocalDate.now().getYear() : year;
        return ResponseEntity.ok(leaveBalanceService.getBalancesForEmployee(user.getId(), y));
    }
}
