package com.exprivia.leave_management.service;

import com.exprivia.leave_management.entity.Employee;
import com.exprivia.leave_management.repository.EmployeeRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
   @Autowired
   private EmployeeRepository employeeRepository;

   public Employee saveEmployee(Employee employee) {
      return (Employee)this.employeeRepository.findBySerialNumber(employee.getSerialNumber()).map((existingEmployee) -> {
         System.out.println("Dipendente " + employee.getSerialNumber() + " già presente.");
         return existingEmployee;
      }).orElseGet(() -> {
         System.out.println("Salvataggio nuovo dipendente: " + employee.getSerialNumber());
         return (Employee)this.employeeRepository.save(employee);
      });
   }

   public List<Employee> getAllEmployees() {
      return this.employeeRepository.findAll();
   }
}
