package com.exprivia.leave_management.dto;

import lombok.Data;

@Data
public class LeaveRequestResponseDTO {
    private Long requestId;
    private String leaveType;
    private String startDate;
    private String endDate;
    private String leaveStatus;
    private String reason;
    private String rejectionReason;
    private EmployeeResponseDTO employee;
    private boolean readByManager;
    private boolean readByEmployee;
}
