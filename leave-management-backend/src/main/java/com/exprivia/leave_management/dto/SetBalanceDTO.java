package com.exprivia.leave_management.dto;

import java.math.BigDecimal;

import com.exprivia.leave_management.entity.LeaveType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetBalanceDTO {

    @NotNull(message = "Il tipo di permesso è obbligatorio")
    private LeaveType leaveType;

    @NotNull(message = "L'anno di riferimento è obbligatorio")
    @Min(value = 1900, message = "Inserire un anno valido")
    private Integer referenceYear;

    @NotNull(message = "La quantità totale è obbligatoria")
    @Min(value = 0, message = "La quantità totale non può essere negativa")
    private BigDecimal totalQuantity;

}
