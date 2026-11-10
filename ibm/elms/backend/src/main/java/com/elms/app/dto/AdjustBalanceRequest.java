package com.elms.app.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdjustBalanceRequest {
    @NotNull
    private Long employeeId;
    @NotNull
    private Long leaveTypeId;
    @NotNull
    private Integer year;
    @NotNull
    private BigDecimal deltaDays;
    @NotBlank
    private String reason;
}
