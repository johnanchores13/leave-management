package com.exprivia.leave_management.controller;

import com.exprivia.leave_management.dto.AdminEmployeeDTO;
import com.exprivia.leave_management.dto.AllBalanceResponseDTO;
import com.exprivia.leave_management.dto.LeaveBalanceResponseDTO;
import com.exprivia.leave_management.dto.RegisterRequest;
import com.exprivia.leave_management.dto.SetBalanceDTO;
import com.exprivia.leave_management.dto.UpdateEmployeeDTO;
import com.exprivia.leave_management.entity.Department;
import com.exprivia.leave_management.entity.Holiday;
import com.exprivia.leave_management.exception.ResourceNotFoundException;
import com.exprivia.leave_management.repository.DepartmentRepository;
import com.exprivia.leave_management.repository.HolidayRepository;
import com.exprivia.leave_management.service.AdminService;
import com.exprivia.leave_management.service.AuthService;
import com.exprivia.leave_management.service.LeaveBalanceService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

   @Autowired
   private LeaveBalanceService leaveBalanceService;

   @Autowired
   private DepartmentRepository departmentRepository;

   @Autowired
   private AdminService adminService;

   @Autowired
   private AuthService authService;

   @Autowired
   private HolidayRepository holidayRepository;

   @GetMapping("/employees")
   public ResponseEntity<Page<AdminEmployeeDTO>> getAllEmployees(
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "10") int size) {
      Pageable pageable = PageRequest.of(page, size);
      return ResponseEntity.ok(adminService.getAllEmployees(pageable));
   }

   @GetMapping("/managers")
   public ResponseEntity<List<AdminEmployeeDTO>> getManagers() {
      return ResponseEntity.ok(adminService.getManagers());
   }

   @PostMapping("/employees")
   public ResponseEntity<String> createEmployee(@Valid @RequestBody RegisterRequest request) {
      authService.createEmployee(request);
      return ResponseEntity.ok("Dipendente creato con successo.");
   }

   @PutMapping("/employees/{employeeId}")
   public ResponseEntity<String> updateEmployee(@PathVariable Long employeeId,
         @Valid @RequestBody UpdateEmployeeDTO updateEmployeeDTO) {
      adminService.updateEmployee(employeeId, updateEmployeeDTO);
      return ResponseEntity.ok("Dipendente aggiornato con successo.");
   }

   @GetMapping("/departments")
   public ResponseEntity<List<Department>> getAllDepartments() {
      return ResponseEntity.ok(departmentRepository.findAll());
   }

   @PostMapping("/departments")
   public ResponseEntity<String> createDepartment(@RequestBody java.util.Map<String, String> body) {
      adminService.createDepartment(body.get("name"));
      return ResponseEntity.ok("Reparto creato con successo.");
   }

   @DeleteMapping("/employees/{employeeId}")
   public ResponseEntity<String> deleteEmployee(@PathVariable Long employeeId) {
      adminService.deleteEmployee(employeeId);
      return ResponseEntity.ok("Dipendente eliminato con successo.");
   }

   @PutMapping("/employees/{employeeId}/balance")
   public ResponseEntity<String> setBalance(
         @PathVariable Long employeeId,
         @Valid @RequestBody SetBalanceDTO setBalanceDTO) {

      leaveBalanceService.setBalance(
            employeeId,
            setBalanceDTO.getLeaveType(),
            setBalanceDTO.getReferenceYear(),
            setBalanceDTO.getTotalQuantity());

      return ResponseEntity.ok("Saldo aggiornato con successo.");
   }

   @GetMapping("/employees/{employeeId}/balance")
   public ResponseEntity<List<LeaveBalanceResponseDTO>> getSaldoDipendente(@PathVariable Long employeeId) {
      return ResponseEntity.ok(leaveBalanceService.getBalanceByEmployee(employeeId));
   }

   @GetMapping("/holidays")
   public ResponseEntity<List<Holiday>> getHolidays() {
      return ResponseEntity.ok(holidayRepository.findAll());
   }

   @PostMapping("/holidays")
   public ResponseEntity<Holiday> addHoliday(@RequestBody Holiday holiday) {
      return ResponseEntity.ok(holidayRepository.save(holiday));
   }

   @DeleteMapping("/holidays/{id}")
   public ResponseEntity<String> deleteHoliday(@PathVariable Long id) {
      holidayRepository.deleteById(id);
      return ResponseEntity.ok("Festività eliminata.");
   }

   @PutMapping("/holidays/{id}")
   public ResponseEntity<Holiday> updateHoliday(@PathVariable Long id, @RequestBody Holiday updatedHoliday) {
      Holiday holiday = holidayRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Festività non trovata."));
      holiday.setDate(updatedHoliday.getDate());
      holiday.setDescription(updatedHoliday.getDescription());
      return ResponseEntity.ok(holidayRepository.save(holiday));
   }

   @PutMapping("/departments/{id}")
   public ResponseEntity<Department> updateDepartment(@PathVariable Long id,
         @RequestBody Department updatedDepartment) {
      Department dept = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reparto non trovato."));
      dept.setName(updatedDepartment.getName());
      return ResponseEntity.ok(departmentRepository.save(dept));
   }

   @DeleteMapping("/departments/{id}")
   public ResponseEntity<String> deleteDepartment(@PathVariable Long id) {
      departmentRepository.deleteById(id);
      return ResponseEntity.ok("Reparto eliminato.");
   }

   @GetMapping("/balances")
   public ResponseEntity<List<AllBalanceResponseDTO>> getAllBalances() {
      return ResponseEntity.ok(leaveBalanceService.getAllBalances());
   }

}
