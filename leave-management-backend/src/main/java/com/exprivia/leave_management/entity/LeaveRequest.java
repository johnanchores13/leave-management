package com.exprivia.leave_management.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Data
public class LeaveRequest {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "request_id")
   private Long requestId;

   @ManyToOne
   @JoinColumn(name = "employee_id", nullable = false)
   private Employee employee;

   @Enumerated(EnumType.STRING)
   private LeaveType leaveType;

   @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
   @Column(name = "start_date")
   private LocalDateTime startDate;

   @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
   @Column(name = "end_date")
   private LocalDateTime endDate;

   @Column(name = "requested_quantity")
   private BigDecimal requestedQuantity;

   private String reason;

   @Enumerated(EnumType.STRING)
   private LeaveStatus leaveStatus;

   @Column(name = "rejection_reason")
   private String rejectionReason;

   @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
   @Column(name = "creation_date", updatable = false)
   private LocalDateTime creationDate;

   @Column(name = "read_by_manager")
   private Boolean readByManager = false;

   @Column(name = "read_by_employee")
   private Boolean readByEmployee = true;
}
