package com.exprivia.leave_management.service;

import com.exprivia.leave_management.dto.ProfileDTO;
import com.exprivia.leave_management.entity.Employee;
import com.exprivia.leave_management.exception.ResourceNotFoundException;
import com.exprivia.leave_management.repository.EmployeeRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmployeeService {
   @Autowired
   private EmployeeRepository employeeRepository;

   public Employee saveEmployee(Employee employee) {
      return (Employee) this.employeeRepository.findBySerialNumber(employee.getSerialNumber())
            .map((existingEmployee) -> {
               log.info("Dipendente {} già presente.", employee.getSerialNumber());
               return existingEmployee;
            }).orElseGet(() -> {
               log.info("Salvataggio nuovo dipendente: {}", employee.getSerialNumber());
               return (Employee) this.employeeRepository.save(employee);
            });
   }

   public List<Employee> getAllEmployees() {
      return this.employeeRepository.findAll();
   }

   public ProfileDTO getProfile(Long employeeId) {
      Employee emp = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Dipendente non trovato."));
      ProfileDTO dto = new ProfileDTO();
      dto.setEmployeeId(emp.getEmployeeId());
      dto.setFirstName(emp.getFirstName());
      dto.setLastName(emp.getLastName());
      dto.setEmail(emp.getEmail());
      dto.setSerialNumber(emp.getSerialNumber());
      dto.setHiringDate(emp.getHiringDate());
      dto.setRoleName(emp.getRole() != null ? emp.getRole().getName() : null);
      dto.setDepartmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : null);
      dto.setManagerFirstName(emp.getManager() != null ? emp.getManager().getFirstName() : null);
      dto.setManagerLastName(emp.getManager() != null ? emp.getManager().getLastName() : null);
      return dto;
   }
}
