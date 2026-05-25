package com.exprivia.leave_management.repository;

import com.exprivia.leave_management.entity.Employee;
import com.exprivia.leave_management.entity.LeaveRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends JpaRepository<LeaveRequest, Long> {
   List<LeaveRequest> findByEmployee(Employee employee);

   List<LeaveRequest> findByEmployee_Manager_EmployeeId(Long managerId);
}
