package com.ibm.elms.service;

import com.ibm.elms.domain.entity.LeaveBalance;
import com.ibm.elms.domain.entity.LeavePolicy;
import com.ibm.elms.domain.entity.LeaveRequest;
import com.ibm.elms.domain.entity.LeaveType;
import com.ibm.elms.domain.entity.UserAccount;
import com.ibm.elms.domain.enums.LeaveRequestStatus;
import com.ibm.elms.domain.enums.Role;
import com.ibm.elms.dto.LeaveDtos.CreateLeaveRequest;
import com.ibm.elms.dto.LeaveDtos.DecisionRequest;
import com.ibm.elms.dto.LeaveDtos.LeaveRequestResponse;
import com.ibm.elms.exception.ApiException;
import com.ibm.elms.repository.LeavePolicyRepository;
import com.ibm.elms.repository.LeaveRequestRepository;
import com.ibm.elms.repository.LeaveTypeRepository;
import com.ibm.elms.repository.UserAccountRepository;
import com.ibm.elms.security.SecurityUtils;
import com.ibm.elms.security.UserPrincipal;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveRequestService {

    private static final List<LeaveRequestStatus> ACTIVE_STATUSES =
            List.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED);

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserAccountRepository userAccountRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final LeaveDurationService leaveDurationService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public LeaveRequestService(
            LeaveRequestRepository leaveRequestRepository,
            UserAccountRepository userAccountRepository,
            LeaveTypeRepository leaveTypeRepository,
            LeavePolicyRepository leavePolicyRepository,
            LeaveBalanceService leaveBalanceService,
            LeaveDurationService leaveDurationService,
            NotificationService notificationService,
            AuditService auditService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userAccountRepository = userAccountRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leavePolicyRepository = leavePolicyRepository;
        this.leaveBalanceService = leaveBalanceService;
        this.leaveDurationService = leaveDurationService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional
    public LeaveRequestResponse submit(CreateLeaveRequest request) {
        UserPrincipal principal = SecurityUtils.currentUser();
        UserAccount employee = userAccountRepository
                .findByIdWithManager(principal.getId())
                .orElseThrow(() -> new ApiException("Employee not found", HttpStatus.NOT_FOUND));

        validateDates(request.startDate(), request.endDate());

        LeaveType leaveType = leaveTypeRepository
                .findById(request.leaveTypeId())
                .filter(LeaveType::isActive)
                .orElseThrow(() -> new ApiException("Leave type not found or inactive", HttpStatus.BAD_REQUEST));

        LeavePolicy policy = leavePolicyRepository
                .findActiveByLeaveTypeId(leaveType.getId())
                .orElseThrow(() -> new ApiException("No active policy for leave type", HttpStatus.BAD_REQUEST));

        BigDecimal duration = leaveDurationService.calculateWorkingDays(
                request.startDate(), request.endDate(), policy);
        if (duration.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Leave duration must include at least one working day", HttpStatus.BAD_REQUEST);
        }
        if (duration.compareTo(BigDecimal.valueOf(policy.getMaxConsecutiveDays())) > 0) {
            throw new ApiException(
                    "Leave exceeds maximum consecutive days allowed by policy", HttpStatus.BAD_REQUEST);
        }

        if (leaveRequestRepository.existsOverlapping(
                employee.getId(), request.startDate(), request.endDate(), ACTIVE_STATUSES, null)) {
            throw new ApiException("Overlapping leave request exists", HttpStatus.CONFLICT);
        }

        int year = request.startDate().getYear();
        LeaveBalance balance = leaveBalanceService.requireBalance(employee.getId(), leaveType.getId(), year);
        if (!policy.isAllowNegativeBalance() && balance.getAvailable().compareTo(duration) < 0) {
            throw new ApiException("Insufficient leave balance", HttpStatus.BAD_REQUEST);
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setManager(employee.getManager());
        leaveRequest.setStartDate(request.startDate());
        leaveRequest.setEndDate(request.endDate());
        leaveRequest.setDurationDays(duration);
        leaveRequest.setReason(request.reason());
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);
        leaveRequestRepository.save(leaveRequest);

        balance.setPending(balance.getPending().add(duration));

        if (employee.getManager() != null) {
            notificationService.notifyUser(
                    employee.getManager().getId(),
                    "New leave request",
                    employee.getFullName() + " submitted a leave request for " + duration + " day(s).");
        }

        auditService.log(
                employee.getId(),
                "SUBMIT_LEAVE",
                "LeaveRequest",
                leaveRequest.getId(),
                "Submitted leave request");

        return Mapper.toRequest(leaveRequestRepository.findDetailedById(leaveRequest.getId()).orElse(leaveRequest));
    }

    @Transactional
    public LeaveRequestResponse decide(Long requestId, boolean approve, DecisionRequest decision) {
        UserPrincipal principal = SecurityUtils.currentUser();
        LeaveRequest leaveRequest = leaveRequestRepository
                .findDetailedById(requestId)
                .orElseThrow(() -> new ApiException("Leave request not found", HttpStatus.NOT_FOUND));

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new ApiException("Only pending requests can be decided", HttpStatus.BAD_REQUEST);
        }
        if (leaveRequest.getEmployee().getId().equals(principal.getId())) {
            throw new ApiException("Employees cannot approve their own requests", HttpStatus.FORBIDDEN);
        }
        if (principal.getRole() == Role.MANAGER) {
            if (leaveRequest.getManager() == null
                    || !leaveRequest.getManager().getId().equals(principal.getId())) {
                throw new ApiException("Only the assigned manager can decide this request", HttpStatus.FORBIDDEN);
            }
        } else if (principal.getRole() != Role.HR_ADMIN && principal.getRole() != Role.SYSTEM_ADMIN) {
            throw new ApiException("Not authorized to decide leave requests", HttpStatus.FORBIDDEN);
        }

        LeaveBalance balance = leaveBalanceService.requireBalance(
                leaveRequest.getEmployee().getId(),
                leaveRequest.getLeaveType().getId(),
                leaveRequest.getStartDate().getYear());

        BigDecimal duration = leaveRequest.getDurationDays();
        balance.setPending(balance.getPending().subtract(duration));

        if (approve) {
            if (balance.getAvailable().compareTo(duration) < 0) {
                throw new ApiException("Insufficient leave balance at approval time", HttpStatus.BAD_REQUEST);
            }
            leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
            balance.setUsed(balance.getUsed().add(duration));
        } else {
            leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
        }

        leaveRequest.setManagerComment(decision == null ? null : decision.comment());
        leaveRequest.setDecidedAt(Instant.now());

        notificationService.notifyUser(
                leaveRequest.getEmployee().getId(),
                "Leave request " + leaveRequest.getStatus().name().toLowerCase(),
                "Your leave request from "
                        + leaveRequest.getStartDate()
                        + " to "
                        + leaveRequest.getEndDate()
                        + " was "
                        + leaveRequest.getStatus().name().toLowerCase()
                        + ".");

        auditService.log(
                principal.getId(),
                approve ? "APPROVE_LEAVE" : "REJECT_LEAVE",
                "LeaveRequest",
                leaveRequest.getId(),
                leaveRequest.getManagerComment());

        return Mapper.toRequest(leaveRequest);
    }

    @Transactional
    public LeaveRequestResponse cancel(Long requestId) {
        UserPrincipal principal = SecurityUtils.currentUser();
        LeaveRequest leaveRequest = leaveRequestRepository
                .findDetailedById(requestId)
                .orElseThrow(() -> new ApiException("Leave request not found", HttpStatus.NOT_FOUND));

        boolean owner = leaveRequest.getEmployee().getId().equals(principal.getId());
        boolean hr = principal.getRole() == Role.HR_ADMIN || principal.getRole() == Role.SYSTEM_ADMIN;
        if (!owner && !hr) {
            throw new ApiException("Not authorized to cancel this request", HttpStatus.FORBIDDEN);
        }
        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING
                && leaveRequest.getStatus() != LeaveRequestStatus.APPROVED) {
            throw new ApiException("Only pending or approved requests can be cancelled", HttpStatus.BAD_REQUEST);
        }

        LeavePolicy policy = leavePolicyRepository
                .findActiveByLeaveTypeId(leaveRequest.getLeaveType().getId())
                .orElse(null);

        LeaveBalance balance = leaveBalanceService.requireBalance(
                leaveRequest.getEmployee().getId(),
                leaveRequest.getLeaveType().getId(),
                leaveRequest.getStartDate().getYear());

        BigDecimal duration = leaveRequest.getDurationDays();
        if (leaveRequest.getStatus() == LeaveRequestStatus.PENDING) {
            balance.setPending(balance.getPending().subtract(duration));
        } else if (leaveRequest.getStatus() == LeaveRequestStatus.APPROVED) {
            boolean restore = policy == null || policy.isRestoreBalanceOnCancel();
            if (restore) {
                balance.setUsed(balance.getUsed().subtract(duration));
            }
        }

        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);
        leaveRequest.setUpdatedAt(Instant.now());

        auditService.log(
                principal.getId(),
                "CANCEL_LEAVE",
                "LeaveRequest",
                leaveRequest.getId(),
                "Cancelled leave request");

        return Mapper.toRequest(leaveRequest);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> myRequests() {
        return leaveRequestRepository.findByEmployeeId(SecurityUtils.currentUser().getId()).stream()
                .map(Mapper::toRequest)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> pendingForManager() {
        return leaveRequestRepository
                .findByManagerIdAndStatus(SecurityUtils.currentUser().getId(), LeaveRequestStatus.PENDING)
                .stream()
                .map(Mapper::toRequest)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> teamHistory() {
        return leaveRequestRepository.findByManagerId(SecurityUtils.currentUser().getId()).stream()
                .map(Mapper::toRequest)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> teamSchedule(LocalDate start, LocalDate end) {
        LocalDate from = start == null ? LocalDate.now().withDayOfMonth(1) : start;
        LocalDate to = end == null ? from.plusMonths(1).minusDays(1) : end;
        return leaveRequestRepository
                .findTeamApprovedInRange(SecurityUtils.currentUser().getId(), from, to)
                .stream()
                .map(Mapper::toRequest)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> all() {
        return leaveRequestRepository.findAllDetailed().stream().map(Mapper::toRequest).toList();
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new ApiException("Start and end dates are required", HttpStatus.BAD_REQUEST);
        }
        if (start.isAfter(end)) {
            throw new ApiException("Start date must be on or before end date", HttpStatus.BAD_REQUEST);
        }
        if (start.isBefore(LocalDate.now())) {
            throw new ApiException("Leave cannot start in the past", HttpStatus.BAD_REQUEST);
        }
    }
}
