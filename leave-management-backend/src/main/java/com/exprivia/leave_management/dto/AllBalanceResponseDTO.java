package com.exprivia.leave_management.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AllBalanceResponseDTO {
    private Long employeeId;
    private String employeeFullName;
    private String leaveType;
    private Integer referenceYear;
    private BigDecimal totalQuantity;
    private BigDecimal usedQuantity;
    private BigDecimal remainingBalance;
}
