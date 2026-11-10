package com.elms.app.controller;

import com.elms.app.dto.*;
import com.elms.app.entity.User;
import com.elms.app.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final UserService userService;

    /** Employee submits a new leave request */
    @PostMapping
    public ResponseEntity<LeaveRequestResponse> submit(@Valid @RequestBody LeaveRequestDto dto,
                                                       Authentication auth) {
        User employee = userService.findByEmail(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveRequestService.submit(dto, employee));
    }

    /** Employee views their own requests */
    @GetMapping("/my")
    public ResponseEntity<List<LeaveRequestResponse>> myRequests(Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(leaveRequestService.getMyRequests(user));
    }

    /** Employee cancels their own request */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<LeaveRequestResponse> cancel(@PathVariable Long id, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(leaveRequestService.cancel(id, user));
    }

    /** Get single request details */
    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequestResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.getById(id));
    }
}
