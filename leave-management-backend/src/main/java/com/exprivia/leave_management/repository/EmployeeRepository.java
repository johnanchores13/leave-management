package com.exprivia.leave_management.repository;

import com.exprivia.leave_management.entity.Employee;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
   Optional<Employee> findBySerialNumber(String serialNumber);

   Optional<Employee> findByEmail(String email);

   List<Employee> findByRole_Name(String roleName);

}
