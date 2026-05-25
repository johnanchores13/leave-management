package com.exprivia.leave_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "leave_balances")
@Data
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Long balanceId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "reference_year")
    private Integer referenceYear;

    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;

    @Column(name = "total_quantity")
    private BigDecimal totalQuantity;

    @Column(name = "usedQuantity")
    private BigDecimal usedQuantity;

    @Transient
    public BigDecimal getRemainingBalance() {
        if (this.totalQuantity == null) return BigDecimal.ZERO;
        if (this.usedQuantity == null) return this.totalQuantity;

        return this.totalQuantity.subtract(this.usedQuantity);
    }

}
