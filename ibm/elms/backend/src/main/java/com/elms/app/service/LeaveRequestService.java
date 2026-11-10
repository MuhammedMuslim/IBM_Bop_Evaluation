package com.elms.app.service;

import com.elms.app.dto.*;
import com.elms.app.entity.*;
import com.elms.app.enums.LeaveStatus;
import com.elms.app.exception.*;
import com.elms.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository    leaveTypeRepository;
    private final LeavePolicyRepository  leavePolicyRepository;
    private final UserService            userService;
    private final WorkingDaysCalculator  workingDaysCalculator;
    private final AuditService           auditService;
    private final EmailService           emailService;

    @Transactional
    public LeaveRequestResponse submit(LeaveRequestDto dto, User employee) {
        // RULE-02: start <= end
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BusinessException("Start date must be before or equal to end date.");
        }
        // RULE-03: no past dates
        if (dto.getStartDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Leave requests cannot include past dates.");
        }

        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));

        LeavePolicy policy = leavePolicyRepository
                .findByLeaveTypeIdAndActiveTrue(dto.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("No active policy for leave type"));

        // RULE min advance notice
        if (dto.getStartDate().isBefore(LocalDate.now().plusDays(policy.getMinAdvanceNoticeDays()))) {
            throw new BusinessException("Minimum advance notice of " + policy.getMinAdvanceNoticeDays() + " day(s) required.");
        }

        // RULE-05: no overlap
        List<LeaveRequest> overlapping = leaveRequestRepository
                .findOverlapping(employee.getId(), dto.getStartDate(), dto.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new BusinessException("You already have an active leave request overlapping these dates.");
        }

        // Calculate working days
        BigDecimal workingDays = workingDaysCalculator.calculate(
                dto.getStartDate(), dto.getEndDate(),
                policy.isExcludeWeekends(), policy.isExcludePublicHolidays());

        if (workingDays.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("The selected dates contain no working days.");
        }

        // Max consecutive check
        if (policy.getMaxConsecutiveDays() != null &&
                workingDays.compareTo(BigDecimal.valueOf(policy.getMaxConsecutiveDays())) > 0) {
            throw new BusinessException("Exceeds maximum consecutive days allowed: " + policy.getMaxConsecutiveDays());
        }

        // RULE-04: sufficient balance
        int year = dto.getStartDate().getYear();
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employee.getId(), leaveType.getId(), year)
                .orElseThrow(() -> new BusinessException("No leave balance found for " + leaveType.getName() + " in " + year));

        if (balance.getAvailableDays().compareTo(workingDays) < 0) {
            throw new BusinessException("Insufficient leave balance. Available: " + balance.getAvailableDays() + " days.");
        }

        // Reserve pending days (atomic)
        balance.setPendingDays(balance.getPendingDays().add(workingDays));
        leaveBalanceRepository.save(balance);

        LeaveRequest request = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .workingDays(workingDays)
                .status(LeaveStatus.PENDING)
                .reason(dto.getReason())
                .build();
        request = leaveRequestRepository.save(request);

        auditService.log(employee, "SUBMIT_LEAVE", "LeaveRequest", request.getId(),
                "Submitted " + leaveType.getName() + " from " + dto.getStartDate() + " to " + dto.getEndDate());

        // Notify manager
        if (employee.getManager() != null) {
            emailService.sendLeaveSubmittedToManager(employee.getManager(), employee, request);
        }

        return toResponse(request);
    }

    @Transactional
    public LeaveRequestResponse review(Long requestId, ReviewRequest dto, User manager) {
        LeaveRequest request = findById(requestId);

        // RULE-07: only assigned manager
        if (request.getEmployee().getManager() == null ||
                !request.getEmployee().getManager().getId().equals(manager.getId())) {
            throw new BusinessException("You are not the assigned manager for this request.");
        }
        // RULE-08: only pending
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("Only PENDING requests can be reviewed.");
        }

        String action = dto.getAction().toUpperCase();
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(
                        request.getEmployee().getId(),
                        request.getLeaveType().getId(),
                        request.getStartDate().getYear())
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found"));

        // Release pending reservation
        balance.setPendingDays(balance.getPendingDays().subtract(request.getWorkingDays())
                .max(BigDecimal.ZERO));

        if ("APPROVE".equals(action)) {
            // RULE-09: deduct balance
            balance.setUsedDays(balance.getUsedDays().add(request.getWorkingDays()));
            request.setStatus(LeaveStatus.APPROVED);
        } else if ("REJECT".equals(action)) {
            request.setStatus(LeaveStatus.REJECTED);
        } else {
            throw new BusinessException("Invalid action. Use APPROVE or REJECT.");
        }

        leaveBalanceRepository.save(balance);
        request.setManagerComment(dto.getComment());
        request.setReviewedBy(manager);
        request.setReviewedAt(LocalDateTime.now());
        request = leaveRequestRepository.save(request);

        auditService.log(manager, action + "_LEAVE", "LeaveRequest", requestId,
                "Manager " + manager.getFullName() + " " + action.toLowerCase() + "d request " + requestId);

        emailService.sendLeaveDecisionToEmployee(request.getEmployee(), request);
        return toResponse(request);
    }

    @Transactional
    public LeaveRequestResponse cancel(Long requestId, User actor) {
        LeaveRequest request = findById(requestId);

        if (!request.getEmployee().getId().equals(actor.getId())) {
            throw new BusinessException("You can only cancel your own requests.");
        }
        if (request.getStatus() == LeaveStatus.CANCELLED) {
            throw new BusinessException("Request is already cancelled.");
        }
        if (request.getStatus() == LeaveStatus.REJECTED) {
            throw new BusinessException("Cannot cancel a rejected request.");
        }

        // Check policy allows cancellation (use a final alias — request is reassigned later)
        final LeaveRequest requestSnapshot = request;
        leavePolicyRepository.findByLeaveTypeIdAndActiveTrue(requestSnapshot.getLeaveType().getId())
                .ifPresent(policy -> {
                    if (!policy.isAllowCancellation()) {
                        throw new BusinessException("Cancellation is not allowed for " + requestSnapshot.getLeaveType().getName());
                    }
                });

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(
                        actor.getId(),
                        request.getLeaveType().getId(),
                        request.getStartDate().getYear())
                .orElse(null);

        if (balance != null) {
            if (request.getStatus() == LeaveStatus.PENDING) {
                balance.setPendingDays(balance.getPendingDays().subtract(request.getWorkingDays()).max(BigDecimal.ZERO));
            } else if (request.getStatus() == LeaveStatus.APPROVED) {
                // RULE-11: restore balance
                balance.setUsedDays(balance.getUsedDays().subtract(request.getWorkingDays()).max(BigDecimal.ZERO));
            }
            leaveBalanceRepository.save(balance);
        }

        request.setStatus(LeaveStatus.CANCELLED);
        request = leaveRequestRepository.save(request);
        auditService.log(actor, "CANCEL_LEAVE", "LeaveRequest", requestId, "Cancelled leave request " + requestId);
        return toResponse(request);
    }

    public List<LeaveRequestResponse> getMyRequests(User employee) {
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employee.getId())
                .stream().map(this::toResponse).toList();
    }

    public List<LeaveRequestResponse> getPendingForManager(User manager) {
        return leaveRequestRepository.findPendingByManagerId(manager.getId())
                .stream().map(this::toResponse).toList();
    }

    public List<LeaveRequestResponse> getAllForManager(User manager) {
        return leaveRequestRepository.findAllByManagerId(manager.getId())
                .stream().map(this::toResponse).toList();
    }

    public List<LeaveRequestResponse> getAll() {
        return leaveRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    public LeaveRequestResponse getById(Long id) {
        return toResponse(findById(id));
    }

    private LeaveRequest findById(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + id));
    }

    public LeaveRequestResponse toResponse(LeaveRequest r) {
        return LeaveRequestResponse.builder()
                .id(r.getId())
                .employeeId(r.getEmployee().getId())
                .employeeName(r.getEmployee().getFullName())
                .leaveTypeId(r.getLeaveType().getId())
                .leaveTypeName(r.getLeaveType().getName())
                .startDate(r.getStartDate())
                .endDate(r.getEndDate())
                .workingDays(r.getWorkingDays())
                .status(r.getStatus())
                .reason(r.getReason())
                .managerComment(r.getManagerComment())
                .reviewedByName(r.getReviewedBy() != null ? r.getReviewedBy().getFullName() : null)
                .reviewedAt(r.getReviewedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
