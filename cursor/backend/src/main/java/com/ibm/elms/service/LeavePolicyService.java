package com.ibm.elms.service;

import com.ibm.elms.domain.entity.LeavePolicy;
import com.ibm.elms.domain.entity.LeaveType;
import com.ibm.elms.dto.LeaveDtos.LeavePolicyRequest;
import com.ibm.elms.dto.LeaveDtos.LeavePolicyResponse;
import com.ibm.elms.exception.ApiException;
import com.ibm.elms.repository.LeavePolicyRepository;
import com.ibm.elms.repository.LeaveTypeRepository;
import com.ibm.elms.security.SecurityUtils;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeavePolicyService {

    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final AuditService auditService;

    public LeavePolicyService(
            LeavePolicyRepository leavePolicyRepository,
            LeaveTypeRepository leaveTypeRepository,
            AuditService auditService) {
        this.leavePolicyRepository = leavePolicyRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<LeavePolicyResponse> list() {
        return leavePolicyRepository.findAll().stream().map(Mapper::toPolicy).toList();
    }

    @Transactional
    public LeavePolicyResponse create(LeavePolicyRequest request) {
        if (leavePolicyRepository.existsByNameIgnoreCase(request.name())) {
            throw new ApiException("Policy name already exists", HttpStatus.CONFLICT);
        }
        LeavePolicy policy = new LeavePolicy();
        apply(policy, request);
        leavePolicyRepository.save(policy);
        auditService.log(
                SecurityUtils.currentUser().getId(),
                "CREATE_POLICY",
                "LeavePolicy",
                policy.getId(),
                "Created policy " + policy.getName());
        return Mapper.toPolicy(policy);
    }

    @Transactional
    public LeavePolicyResponse update(Long id, LeavePolicyRequest request) {
        LeavePolicy policy = leavePolicyRepository
                .findById(id)
                .orElseThrow(() -> new ApiException("Policy not found", HttpStatus.NOT_FOUND));
        apply(policy, request);
        auditService.log(
                SecurityUtils.currentUser().getId(),
                "UPDATE_POLICY",
                "LeavePolicy",
                policy.getId(),
                "Updated policy " + policy.getName());
        return Mapper.toPolicy(policy);
    }

    private void apply(LeavePolicy policy, LeavePolicyRequest request) {
        LeaveType leaveType = leaveTypeRepository
                .findById(request.leaveTypeId())
                .orElseThrow(() -> new ApiException("Leave type not found", HttpStatus.NOT_FOUND));
        policy.setName(request.name());
        policy.setLeaveType(leaveType);
        policy.setAnnualEntitlementDays(request.annualEntitlementDays());
        policy.setExcludeWeekends(request.excludeWeekends());
        policy.setExcludePublicHolidays(request.excludePublicHolidays());
        policy.setAllowNegativeBalance(request.allowNegativeBalance());
        policy.setRestoreBalanceOnCancel(request.restoreBalanceOnCancel());
        policy.setMaxConsecutiveDays(request.maxConsecutiveDays());
        policy.setActive(request.active());
    }
}
