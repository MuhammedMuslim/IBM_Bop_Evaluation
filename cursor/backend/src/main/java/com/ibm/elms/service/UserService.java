package com.ibm.elms.service;

import com.ibm.elms.domain.entity.LeaveBalance;
import com.ibm.elms.domain.entity.LeavePolicy;
import com.ibm.elms.domain.entity.UserAccount;
import com.ibm.elms.domain.enums.EmployeeStatus;
import com.ibm.elms.dto.UserDtos.CreateUserRequest;
import com.ibm.elms.dto.UserDtos.UpdateUserRequest;
import com.ibm.elms.dto.UserDtos.UserResponse;
import com.ibm.elms.exception.ApiException;
import com.ibm.elms.repository.LeaveBalanceRepository;
import com.ibm.elms.repository.LeavePolicyRepository;
import com.ibm.elms.repository.UserAccountRepository;
import com.ibm.elms.security.SecurityUtils;
import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(
            UserAccountRepository userAccountRepository,
            LeavePolicyRepository leavePolicyRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.userAccountRepository = userAccountRepository;
        this.leavePolicyRepository = leavePolicyRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAll() {
        return userAccountRepository.findAll().stream()
                .map(u -> userAccountRepository.findByIdWithManager(u.getId()).orElse(u))
                .map(Mapper::toUser)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return Mapper.toUser(requireUser(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> teamMembers(Long managerId) {
        return userAccountRepository.findByManagerIdAndStatus(managerId, EmployeeStatus.ACTIVE).stream()
                .map(Mapper::toUser)
                .toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException("Email already registered", HttpStatus.CONFLICT);
        }
        UserAccount user = new UserAccount();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmployeeCode(request.employeeCode());
        user.setDepartment(request.department());
        user.setJobTitle(request.jobTitle());
        user.setRole(request.role());
        user.setStatus(EmployeeStatus.ACTIVE);
        if (request.managerId() != null) {
            user.setManager(requireUser(request.managerId()));
        }
        userAccountRepository.save(user);
        assignDefaultBalances(user);
        auditService.log(
                SecurityUtils.currentUser().getId(),
                "CREATE_USER",
                "User",
                user.getId(),
                "Created user " + user.getEmail());
        return Mapper.toUser(requireUser(user.getId()));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        UserAccount user = requireUser(id);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmployeeCode(request.employeeCode());
        user.setDepartment(request.department());
        user.setJobTitle(request.jobTitle());
        user.setRole(request.role());
        user.setStatus(request.status());
        if (request.managerId() == null) {
            user.setManager(null);
        } else {
            if (request.managerId().equals(id)) {
                throw new ApiException("Employee cannot be their own manager", HttpStatus.BAD_REQUEST);
            }
            user.setManager(requireUser(request.managerId()));
        }
        auditService.log(
                SecurityUtils.currentUser().getId(),
                "UPDATE_USER",
                "User",
                user.getId(),
                "Updated user " + user.getEmail());
        return Mapper.toUser(requireUser(user.getId()));
    }

    private void assignDefaultBalances(UserAccount user) {
        int year = Year.now().getValue();
        for (LeavePolicy policy : leavePolicyRepository.findByActiveTrue()) {
            LeaveBalance balance = new LeaveBalance();
            balance.setEmployee(user);
            balance.setLeaveType(policy.getLeaveType());
            balance.setYear(year);
            balance.setEntitled(BigDecimal.valueOf(policy.getAnnualEntitlementDays()));
            leaveBalanceRepository.save(balance);
        }
    }

    private UserAccount requireUser(Long id) {
        return userAccountRepository
                .findByIdWithManager(id)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }
}
