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

   @Scheduled(
      cron = "0 0 0 1 * ?"
   )
   @Transactional
   public void accreditRateiMensili() {
      int annoCorrente = LocalDate.now().getYear();
      List<Employee> dipendenti = this.employeeRepository.findAll();
      BigDecimal ferieMensili = new BigDecimal("1.67");
      BigDecimal permessiMensili = new BigDecimal("4.00");

      for(Employee dipendente : dipendenti) {
         this.accreditaFerie(dipendente, annoCorrente, LeaveType.VACATION, ferieMensili);
         this.accreditaFerie(dipendente, annoCorrente, LeaveType.PERMIT, permessiMensili);
      }

   }

   private void accreditaFerie(Employee dipendente, int anno, LeaveType tipo, BigDecimal quantita) {
      LeaveBalance saldo = (LeaveBalance)this.balanceRepository.findByEmployeeAndReferenceYearAndLeaveType(dipendente, anno, tipo).orElseGet(() -> {
         LeaveBalance nuovoSaldo = new LeaveBalance();
         nuovoSaldo.setEmployee(dipendente);
         nuovoSaldo.setReferenceYear(anno);
         nuovoSaldo.setLeaveType(tipo);
         nuovoSaldo.setUsedQuantity(BigDecimal.ZERO);
         BigDecimal riporto = (BigDecimal)this.balanceRepository.findByEmployeeAndReferenceYearAndLeaveType(dipendente, anno - 1, tipo).map((vecchio) -> vecchio.getTotalQuantity().subtract(vecchio.getUsedQuantity())).filter((residuo) -> residuo.compareTo(BigDecimal.ZERO) > 0).orElse(BigDecimal.ZERO);
         nuovoSaldo.setTotalQuantity(riporto);
         return nuovoSaldo;
      });
      saldo.setTotalQuantity(saldo.getTotalQuantity().add(quantita));
      this.balanceRepository.save(saldo);
   }
}
