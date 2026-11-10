package com.ibm.elms.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "leave_policies")
public class LeavePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id")
    private LeaveType leaveType;

    @Column(nullable = false)
    private int annualEntitlementDays;

    @Column(nullable = false)
    private boolean excludeWeekends = true;

    @Column(nullable = false)
    private boolean excludePublicHolidays = true;

    @Column(nullable = false)
    private boolean allowNegativeBalance = false;

    @Column(nullable = false)
    private boolean restoreBalanceOnCancel = true;

    @Column(nullable = false)
    private int maxConsecutiveDays = 30;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public int getAnnualEntitlementDays() {
        return annualEntitlementDays;
    }

    public void setAnnualEntitlementDays(int annualEntitlementDays) {
        this.annualEntitlementDays = annualEntitlementDays;
    }

    public boolean isExcludeWeekends() {
        return excludeWeekends;
    }

    public void setExcludeWeekends(boolean excludeWeekends) {
        this.excludeWeekends = excludeWeekends;
    }

    public boolean isExcludePublicHolidays() {
        return excludePublicHolidays;
    }

    public void setExcludePublicHolidays(boolean excludePublicHolidays) {
        this.excludePublicHolidays = excludePublicHolidays;
    }

    public boolean isAllowNegativeBalance() {
        return allowNegativeBalance;
    }

    public void setAllowNegativeBalance(boolean allowNegativeBalance) {
        this.allowNegativeBalance = allowNegativeBalance;
    }

    public boolean isRestoreBalanceOnCancel() {
        return restoreBalanceOnCancel;
    }

    public void setRestoreBalanceOnCancel(boolean restoreBalanceOnCancel) {
        this.restoreBalanceOnCancel = restoreBalanceOnCancel;
    }

    public int getMaxConsecutiveDays() {
        return maxConsecutiveDays;
    }

    public void setMaxConsecutiveDays(int maxConsecutiveDays) {
        this.maxConsecutiveDays = maxConsecutiveDays;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
