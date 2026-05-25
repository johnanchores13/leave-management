package com.exprivia.leave_management.dto;

import lombok.Data;

@Data
public class EmployeeResponseDTO {
   private Long employeeId;
   private String firstName;
   private String lastName;
   private String role;

}
