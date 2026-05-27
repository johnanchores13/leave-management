package com.exprivia.leave_management.service;

import com.exprivia.leave_management.entity.Employee;
import com.exprivia.leave_management.entity.LeaveBalance;
import com.exprivia.leave_management.entity.LeaveType;
import com.exprivia.leave_management.repository.BalanceRepository;
import com.exprivia.leave_management.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveAccrualService {
   @Autowired
   private EmployeeRepository employeeRepository;
   @Autowired
   private BalanceRepository balanceRepository;

   @Scheduled(cron = "0 0 0 1 * ?")
   @Transactional
   public void creditMonthlyAccruals() {
      int currentYear = LocalDate.now().getYear();
      List<Employee> employees = this.employeeRepository.findAll();
      BigDecimal monthlyVacation = new BigDecimal("1.67");
      BigDecimal monthlyPermit = new BigDecimal("4.00");

      for (Employee employee : employees) {
         this.creditLeave(employee, currentYear, LeaveType.VACATION, monthlyVacation);
         this.creditLeave(employee, currentYear, LeaveType.PERMIT, monthlyPermit);
      }

   }

   private void creditLeave(Employee employee, int year, LeaveType leaveType, BigDecimal quantity) {
      LeaveBalance balance = (LeaveBalance) this.balanceRepository
            .findByEmployeeAndReferenceYearAndLeaveType(employee, year, leaveType).orElseGet(() -> {
               LeaveBalance newBalance = new LeaveBalance();
               newBalance.setEmployee(employee);
               newBalance.setReferenceYear(year);
               newBalance.setLeaveType(leaveType);
               newBalance.setUsedQuantity(BigDecimal.ZERO);
               BigDecimal carryOver = (BigDecimal) this.balanceRepository
                     .findByEmployeeAndReferenceYearAndLeaveType(employee, year - 1, leaveType)
                     .map((previousBalance) -> previousBalance.getTotalQuantity()
                           .subtract(previousBalance.getUsedQuantity()))
                     .filter((remaining) -> remaining.compareTo(BigDecimal.ZERO) > 0).orElse(BigDecimal.ZERO);
               newBalance.setTotalQuantity(carryOver);
               return newBalance;
            });
      balance.setTotalQuantity(balance.getTotalQuantity().add(quantity));
      this.balanceRepository.save(balance);
   }
}
