package com.elms.app.controller;

import com.elms.app.entity.LeaveType;
import com.elms.app.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-types")
@RequiredArgsConstructor
public class LeaveTypeController {

    private final LeaveTypeRepository leaveTypeRepository;

    @GetMapping
    public ResponseEntity<List<LeaveType>> getActive() {
        return ResponseEntity.ok(leaveTypeRepository.findByActiveTrue());
    }
}
