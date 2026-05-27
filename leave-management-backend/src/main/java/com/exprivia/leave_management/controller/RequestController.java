package com.exprivia.leave_management.controller;

import com.exprivia.leave_management.config.JwtUtil;
import com.exprivia.leave_management.dto.ChangePasswordDTO;
import com.exprivia.leave_management.dto.LeaveBalanceResponseDTO;
import com.exprivia.leave_management.dto.LeaveRequestDTO;
import com.exprivia.leave_management.dto.LeaveRequestResponseDTO;
import com.exprivia.leave_management.dto.ProfileDTO;
import com.exprivia.leave_management.entity.Employee;
import com.exprivia.leave_management.entity.LeaveStatus;
import com.exprivia.leave_management.exception.ResourceNotFoundException;
import com.exprivia.leave_management.exception.UnauthorizedException;
import com.exprivia.leave_management.repository.EmployeeRepository;
import com.exprivia.leave_management.service.EmployeeService;
import com.exprivia.leave_management.service.AuthService;
import com.exprivia.leave_management.service.LeaveAccrualService;
import com.exprivia.leave_management.service.LeaveBalanceService;
import com.exprivia.leave_management.service.LeaveRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ "/api/requests" })
public class RequestController {

   @Autowired
   private LeaveBalanceService leaveBalanceService;

   @Autowired
   private AuthService authService;

   @Autowired
   private JwtUtil jwtUtil;
   @Autowired
   private LeaveRequestService leaveRequestService;
   @Autowired
   private LeaveAccrualService accrualService;

   @Autowired
   private EmployeeService employeeService;

   @Autowired
   private EmployeeRepository employeeRepository;

   @PostMapping({ "/submit" })
   public ResponseEntity<LeaveRequestResponseDTO> sendRequest(@RequestBody @Valid LeaveRequestDTO dto,
         HttpServletRequest request) {
      Long employeeId = this.getAuthenticatedEmployeeId(request);
      LeaveRequestResponseDTO saved = this.leaveRequestService.createDTOLeaveRequest(employeeId, dto);
      return ResponseEntity.ok(saved);
   }

   @GetMapping({ "/employee" })
   public ResponseEntity<List<LeaveRequestResponseDTO>> getMyRequests(HttpServletRequest request) {
      Long employeeId = this.getAuthenticatedEmployeeId(request);
      return ResponseEntity.ok(this.leaveRequestService.getLeaveRequestsByEmployee(employeeId));
   }

   @PreAuthorize("hasRole('RESPONSABILE')")
   @PutMapping("/{requestId}/approve")
   public ResponseEntity<LeaveRequestResponseDTO> approveRequest(
         @PathVariable Long requestId, HttpServletRequest request) {
      Long managerId = getAuthenticatedEmployeeId(request);
      LeaveRequestResponseDTO updated = leaveRequestService.processLeaveRequest(requestId, LeaveStatus.APPROVED, null,
            managerId);
      return ResponseEntity.ok(updated);
   }

   @PreAuthorize("hasRole('RESPONSABILE')")
   @PutMapping("/{requestId}/reject")
   public ResponseEntity<LeaveRequestResponseDTO> rejectRequest(
         @PathVariable Long requestId,
         @RequestParam(required = false) String reason,
         HttpServletRequest request) {
      Long managerId = getAuthenticatedEmployeeId(request);
      LeaveRequestResponseDTO updated = leaveRequestService.processLeaveRequest(requestId, LeaveStatus.REJECTED, reason,
            managerId);
      return ResponseEntity.ok(updated);
   }

   @GetMapping({ "/balance" })
   public ResponseEntity<List<LeaveBalanceResponseDTO>> getBalance(HttpServletRequest request) {
      Long employeeId = this.getAuthenticatedEmployeeId(request);
      return ResponseEntity.ok(this.leaveBalanceService.getBalanceByEmployee(employeeId));
   }

   @PutMapping("/{requestId}/cancel")
   public ResponseEntity<String> cancelRequest(@PathVariable Long requestId, HttpServletRequest request) {
      Long employeeId = getAuthenticatedEmployeeId(request);
      leaveRequestService.cancelLeaveRequest(requestId, employeeId);
      return ResponseEntity.ok("Richiesta cancellata con successo.");
   }

   private Long getAuthenticatedEmployeeId(HttpServletRequest request) {
      String header = request.getHeader("Authorization");
      if (header == null || !header.startsWith("Bearer ")) {
         throw new UnauthorizedException("Token di autenticazione mancante o non valido.");
      }
      String token = header.substring(7);
      return this.jwtUtil.extractEmployeeId(token);
   }

   @PreAuthorize("hasRole('RESPONSABILE')")
   @GetMapping({ "/manager/requests" })
   public ResponseEntity<List<LeaveRequestResponseDTO>> getRequestsForManager(HttpServletRequest request) {
      Long managerId = this.getAuthenticatedEmployeeId(request);
      return ResponseEntity.ok(this.leaveRequestService.getRequestsForManager(managerId));
   }

   @PutMapping("/{requestId}/read")
   @PreAuthorize("hasAnyRole('DIPENDENTE', 'RESPONSABILE')")
   public ResponseEntity<Void> markAsRead(@PathVariable Long requestId, HttpServletRequest request) {
      Long employeeId = getAuthenticatedEmployeeId(request);
      leaveRequestService.markAsRead(requestId, employeeId);
      return ResponseEntity.ok().build();
   }

   @PutMapping("/{requestId}/read-manager")
   @PreAuthorize("hasRole('RESPONSABILE')")
   public ResponseEntity<Void> markAsReadByManager(@PathVariable Long requestId, HttpServletRequest request) {
      Long managerId = getAuthenticatedEmployeeId(request);
      leaveRequestService.markAsReadByManager(requestId, managerId);
      return ResponseEntity.ok().build();
   }

   @PreAuthorize("hasRole('ADMIN')")
   @PostMapping("/manager/simulate-month")
   public ResponseEntity<String> simulaAccreditoMensile() {
      accrualService.creditMonthlyAccruals();
      return ResponseEntity.ok("Ferie e permessi caricati con successo.");
   }

   @PutMapping({ "/change-password" })
   public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordDTO dto, HttpServletRequest request) {
      Long employeeId = this.getAuthenticatedEmployeeId(request);
      String oldPassword = dto.getOldPassword();
      String newPassword = dto.getNewPassword();
      this.authService.changePassword(employeeId, oldPassword, newPassword);
      return ResponseEntity.ok("Password aggiornata con successo.");
   }

   @GetMapping("/me")
   public ResponseEntity<ProfileDTO> getProfile(HttpServletRequest request) {
      Long employeeId = getAuthenticatedEmployeeId(request);
      return ResponseEntity.ok(employeeService.getProfile(employeeId));
   }

   @PreAuthorize("hasRole('RESPONSABILE')")
   @GetMapping("/manager/employees/{employeeId}/balance")
   public ResponseEntity<List<LeaveBalanceResponseDTO>> getEmployeeBalanceByManager(
         @PathVariable Long employeeId,
         HttpServletRequest request) {

      Long managerId = getAuthenticatedEmployeeId(request);

      Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Dipendente non trovato."));

      if (employee.getManager() == null || !employee.getManager().getEmployeeId().equals(managerId)) {
         throw new UnauthorizedException("Non sei autorizzato a vedere il saldo di questo dipendente.");
      }

      return ResponseEntity.ok(leaveBalanceService.getBalanceByEmployee(employeeId));
   }
}
