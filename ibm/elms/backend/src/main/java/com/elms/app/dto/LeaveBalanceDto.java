package com.elms.app.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class LeaveBalanceDto {
    private Long id;
    private Long leaveTypeId;
    private String leaveTypeName;
    private int year;
    private BigDecimal totalDays;
    private BigDecimal usedDays;
    private BigDecimal pendingDays;
    private BigDecimal availableDays;
}
