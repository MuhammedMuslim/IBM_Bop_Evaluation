package com.ibm.elms.controller;

import com.ibm.elms.dto.LeaveDtos.LeaveTypeRequest;
import com.ibm.elms.dto.LeaveDtos.LeaveTypeResponse;
import com.ibm.elms.service.LeaveTypeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-types")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    public LeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @GetMapping
    public List<LeaveTypeResponse> list(@RequestParam(defaultValue = "true") boolean activeOnly) {
        return leaveTypeService.list(activeOnly);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN')")
    public LeaveTypeResponse create(@Valid @RequestBody LeaveTypeRequest request) {
        return leaveTypeService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN')")
    public LeaveTypeResponse update(@PathVariable Long id, @Valid @RequestBody LeaveTypeRequest request) {
        return leaveTypeService.update(id, request);
    }
}
