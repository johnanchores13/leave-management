package com.exprivia.leave_management.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AdminEmployeeDTO {
   private Long employeeId;
   private String serialNumber;
   private String firstName;
   private String lastName;
   private String email;
   private LocalDate hiringDate;
   private String departmentName;
   private String roleName;
   private String managerFirstName;
   private String managerLastName;
   private Long departmentId;
   private Long managerId;
}
