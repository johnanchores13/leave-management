package com.exprivia.leave_management.repository;

import com.exprivia.leave_management.entity.Employee;
import com.exprivia.leave_management.entity.LeaveRequest;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends JpaRepository<LeaveRequest, Long> {
   List<LeaveRequest> findByEmployee(Employee employee);

   List<LeaveRequest> findByEmployee_Manager_EmployeeId(Long managerId);

   @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM LeaveRequest r " +
         "WHERE r.employee = :employee " +
         "AND r.leaveStatus NOT IN ('REJECTED', 'CANCELED') " +
         "AND r.startDate <= :endDate AND r.endDate >= :startDate")
   boolean existsOverlapping(@Param("employee") Employee employee,
         @Param("startDate") LocalDateTime startDate,
         @Param("endDate") LocalDateTime endDate);

}
