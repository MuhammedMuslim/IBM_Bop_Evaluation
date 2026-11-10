package com.ibm.elms.service;

import com.ibm.elms.domain.entity.LeaveType;
import com.ibm.elms.dto.LeaveDtos.LeaveTypeRequest;
import com.ibm.elms.dto.LeaveDtos.LeaveTypeResponse;
import com.ibm.elms.exception.ApiException;
import com.ibm.elms.repository.LeaveTypeRepository;
import com.ibm.elms.security.SecurityUtils;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final AuditService auditService;

    public LeaveTypeService(LeaveTypeRepository leaveTypeRepository, AuditService auditService) {
        this.leaveTypeRepository = leaveTypeRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> list(boolean activeOnly) {
        List<LeaveType> types = activeOnly ? leaveTypeRepository.findByActiveTrue() : leaveTypeRepository.findAll();
        return types.stream().map(Mapper::toLeaveType).toList();
    }

    @Transactional
    public LeaveTypeResponse create(LeaveTypeRequest request) {
        if (leaveTypeRepository.existsByNameIgnoreCase(request.name())) {
            throw new ApiException("Leave type already exists", HttpStatus.CONFLICT);
        }
        LeaveType type = new LeaveType();
        apply(type, request);
        leaveTypeRepository.save(type);
        auditService.log(
                SecurityUtils.currentUser().getId(),
                "CREATE_LEAVE_TYPE",
                "LeaveType",
                type.getId(),
                "Created leave type " + type.getName());
        return Mapper.toLeaveType(type);
    }

    @Transactional
    public LeaveTypeResponse update(Long id, LeaveTypeRequest request) {
        LeaveType type = leaveTypeRepository
                .findById(id)
                .orElseThrow(() -> new ApiException("Leave type not found", HttpStatus.NOT_FOUND));
        apply(type, request);
        auditService.log(
                SecurityUtils.currentUser().getId(),
                "UPDATE_LEAVE_TYPE",
                "LeaveType",
                type.getId(),
                "Updated leave type " + type.getName());
        return Mapper.toLeaveType(type);
    }

    private void apply(LeaveType type, LeaveTypeRequest request) {
        type.setName(request.name());
        type.setDescription(request.description());
        type.setPaid(request.paid());
        type.setRequiresApproval(request.requiresApproval());
        type.setActive(request.active());
    }
}
