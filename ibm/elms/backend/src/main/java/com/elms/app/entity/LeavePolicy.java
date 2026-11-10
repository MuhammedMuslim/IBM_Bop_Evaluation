package com.elms.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_policies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeavePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "annual_entitlement_days", nullable = false)
    private int annualEntitlementDays;

    @Column(name = "carry_over_days", nullable = false)
    private int carryOverDays = 0;

    @Column(name = "max_consecutive_days")
    private Integer maxConsecutiveDays;

    @Column(name = "min_advance_notice_days", nullable = false)
    private int minAdvanceNoticeDays = 0;

    @Column(name = "allow_half_day", nullable = false)
    private boolean allowHalfDay = false;

    @Column(name = "exclude_weekends", nullable = false)
    private boolean excludeWeekends = true;

    @Column(name = "exclude_public_holidays", nullable = false)
    private boolean excludePublicHolidays = true;

    @Column(name = "allow_cancellation", nullable = false)
    private boolean allowCancellation = true;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
