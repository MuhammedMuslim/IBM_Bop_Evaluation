package com.ibm.elms.controller;

import com.ibm.elms.dto.LeaveDtos.LeavePolicyRequest;
import com.ibm.elms.dto.LeaveDtos.LeavePolicyResponse;
import com.ibm.elms.service.LeavePolicyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-policies")
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    public LeavePolicyController(LeavePolicyService leavePolicyService) {
        this.leavePolicyService = leavePolicyService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN','MANAGER')")
    public List<LeavePolicyResponse> list() {
        return leavePolicyService.list();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN')")
    public LeavePolicyResponse create(@Valid @RequestBody LeavePolicyRequest request) {
        return leavePolicyService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_ADMIN','SYSTEM_ADMIN')")
    public LeavePolicyResponse update(@PathVariable Long id, @Valid @RequestBody LeavePolicyRequest request) {
        return leavePolicyService.update(id, request);
    }
}
