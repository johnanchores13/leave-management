package com.exprivia.leave_management.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class LeaveBalanceResponseDTO {
   private String leaveType;
   private Integer referenceYear;
   private BigDecimal totalQuantity;
   private BigDecimal usedQuantity;
   private BigDecimal remainingBalance;

}
