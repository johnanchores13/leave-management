package com.exprivia.leave_management.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exprivia.leave_management.dto.AllBalanceResponseDTO;
import com.exprivia.leave_management.dto.LeaveBalanceResponseDTO;
import com.exprivia.leave_management.entity.Employee;
import com.exprivia.leave_management.entity.LeaveBalance;
import com.exprivia.leave_management.entity.LeaveType;
import com.exprivia.leave_management.exception.ResourceNotFoundException;
import com.exprivia.leave_management.repository.BalanceRepository;
import com.exprivia.leave_management.repository.EmployeeRepository;

@Service
public class LeaveBalanceService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Transactional
    public void setBalance(Long employeeId, LeaveType leaveType, int year, BigDecimal quantity) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dipendente non trovato."));

        LeaveBalance balance = balanceRepository
                .findByEmployeeAndReferenceYearAndLeaveType(employee, year, leaveType)
                .orElseGet(() -> {
                    LeaveBalance newBalance = new LeaveBalance();
                    newBalance.setEmployee(employee);
                    newBalance.setReferenceYear(year);
                    newBalance.setLeaveType(leaveType);
                    newBalance.setUsedQuantity(BigDecimal.ZERO);
                    return newBalance;
                });

        balance.setTotalQuantity(quantity);
        balanceRepository.save(balance);
    }

    public List<LeaveBalanceResponseDTO> getBalanceByEmployee(Long employeeId) {
        Employee employee = (Employee) this.employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dipendente non trovato."));
        return this.balanceRepository.findByEmployee(employee).stream().map((lb) -> {
            LeaveBalanceResponseDTO dto = new LeaveBalanceResponseDTO();
            dto.setLeaveType(lb.getLeaveType().name());
            dto.setReferenceYear(lb.getReferenceYear());
            dto.setTotalQuantity(lb.getTotalQuantity());
            dto.setUsedQuantity(lb.getUsedQuantity());
            dto.setRemainingBalance(lb.getRemainingBalance());
            return dto;
        }).toList();
    }

    public List<AllBalanceResponseDTO> getAllBalances() {
        return balanceRepository.findAll().stream().map(lb -> {
            AllBalanceResponseDTO dto = new AllBalanceResponseDTO();
            dto.setEmployeeId(lb.getEmployee().getEmployeeId());
            dto.setEmployeeFullName(lb.getEmployee().getFirstName() + " " + lb.getEmployee().getLastName());
            dto.setLeaveType(lb.getLeaveType().name());
            dto.setReferenceYear(lb.getReferenceYear());
            dto.setTotalQuantity(lb.getTotalQuantity());
            dto.setUsedQuantity(lb.getUsedQuantity());
            dto.setRemainingBalance(lb.getRemainingBalance());
            return dto;
        }).toList();
    }

}
