package com.exprivia.leave_management.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ProfileDTO {
    private Long employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String serialNumber;
    private LocalDate hiringDate;
    private String roleName;
    private String departmentName;
    private String managerFirstName;
    private String managerLastName;
}
