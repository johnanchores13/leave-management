package com.exprivia.leave_management.repository;

import com.exprivia.leave_management.entity.Employee;
import com.exprivia.leave_management.entity.LeaveBalance;
import com.exprivia.leave_management.entity.LeaveType;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceRepository extends JpaRepository<LeaveBalance, Long> {
   @Lock(LockModeType.PESSIMISTIC_WRITE)
   Optional<LeaveBalance> findByEmployeeAndReferenceYearAndLeaveType(Employee employee, Integer year, LeaveType leaveType);

   List<LeaveBalance> findByEmployee(Employee employee);
}
