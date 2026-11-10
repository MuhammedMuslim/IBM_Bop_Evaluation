package com.ibm.elms.config;

import com.ibm.elms.domain.entity.*;
import com.ibm.elms.domain.enums.EmployeeStatus;
import com.ibm.elms.domain.enums.Role;
import com.ibm.elms.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PublicHolidayRepository publicHolidayRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserAccountRepository userAccountRepository,
            LeaveTypeRepository leaveTypeRepository,
            LeavePolicyRepository leavePolicyRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            PublicHolidayRepository publicHolidayRepository,
            PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leavePolicyRepository = leavePolicyRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.publicHolidayRepository = publicHolidayRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userAccountRepository.count() > 0) {
            return;
        }

        LeaveType annual = leaveType("Annual Leave", "Paid annual vacation leave", true);
        LeaveType sick = leaveType("Sick Leave", "Medical and illness leave", true);
        LeaveType unpaid = leaveType("Unpaid Leave", "Leave without pay", false);

        LeavePolicy annualPolicy = policy("Standard Annual Policy", annual, 20);
        LeavePolicy sickPolicy = policy("Standard Sick Policy", sick, 10);
        LeavePolicy unpaidPolicy = policy("Unpaid Leave Policy", unpaid, 0);
        unpaidPolicy.setAllowNegativeBalance(true);
        leavePolicyRepository.save(unpaidPolicy);

        int year = Year.now().getValue();
        publicHolidayRepository.save(holiday(LocalDate.of(year, 1, 1), "New Year's Day"));
        publicHolidayRepository.save(holiday(LocalDate.of(year, 12, 25), "Christmas Day"));

        UserAccount admin = user("admin@elms.local", "System", "Admin", Role.SYSTEM_ADMIN, "ADM001", null);
        UserAccount hr = user("hr@elms.local", "Helen", "Rivera", Role.HR_ADMIN, "HR001", null);
        UserAccount manager = user("manager@elms.local", "Marcus", "Lee", Role.MANAGER, "MGR001", null);
        UserAccount emp1 = user("alice@elms.local", "Alice", "Nguyen", Role.EMPLOYEE, "EMP001", manager);
        UserAccount emp2 = user("bob@elms.local", "Bob", "Patel", Role.EMPLOYEE, "EMP002", manager);

        for (UserAccount u : new UserAccount[] {admin, hr, manager, emp1, emp2}) {
            seedBalance(u, annual, annualPolicy.getAnnualEntitlementDays(), year);
            seedBalance(u, sick, sickPolicy.getAnnualEntitlementDays(), year);
            seedBalance(u, unpaid, unpaidPolicy.getAnnualEntitlementDays(), year);
        }
    }

    private LeaveType leaveType(String name, String description, boolean paid) {
        LeaveType type = new LeaveType();
        type.setName(name);
        type.setDescription(description);
        type.setPaid(paid);
        type.setActive(true);
        type.setRequiresApproval(true);
        return leaveTypeRepository.save(type);
    }

    private LeavePolicy policy(String name, LeaveType type, int days) {
        LeavePolicy policy = new LeavePolicy();
        policy.setName(name);
        policy.setLeaveType(type);
        policy.setAnnualEntitlementDays(days);
        policy.setExcludeWeekends(true);
        policy.setExcludePublicHolidays(true);
        policy.setAllowNegativeBalance(false);
        policy.setRestoreBalanceOnCancel(true);
        policy.setMaxConsecutiveDays(30);
        policy.setActive(true);
        return leavePolicyRepository.save(policy);
    }

    private UserAccount user(
            String email, String first, String last, Role role, String code, UserAccount manager) {
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setFirstName(first);
        user.setLastName(last);
        user.setRole(role);
        user.setEmployeeCode(code);
        user.setDepartment(role == Role.HR_ADMIN || role == Role.SYSTEM_ADMIN ? "People Ops" : "Engineering");
        user.setJobTitle(role.name().replace('_', ' '));
        user.setStatus(EmployeeStatus.ACTIVE);
        user.setManager(manager);
        return userAccountRepository.save(user);
    }

    private void seedBalance(UserAccount employee, LeaveType type, int entitled, int year) {
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(employee);
        balance.setLeaveType(type);
        balance.setYear(year);
        balance.setEntitled(BigDecimal.valueOf(entitled));
        leaveBalanceRepository.save(balance);
    }

    private PublicHoliday holiday(LocalDate date, String name) {
        PublicHoliday holiday = new PublicHoliday();
        holiday.setDate(date);
        holiday.setName(name);
        return holiday;
    }
}
