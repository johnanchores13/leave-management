package com.exprivia.leave_management.service;

import com.exprivia.leave_management.dto.AllBalanceResponseDTO;
import com.exprivia.leave_management.dto.EmployeeResponseDTO;
import com.exprivia.leave_management.dto.LeaveBalanceResponseDTO;
import com.exprivia.leave_management.dto.LeaveRequestDTO;
import com.exprivia.leave_management.dto.LeaveRequestResponseDTO;
import com.exprivia.leave_management.entity.Employee;
import com.exprivia.leave_management.entity.Holiday;
import com.exprivia.leave_management.entity.LeaveBalance;
import com.exprivia.leave_management.entity.LeaveRequest;
import com.exprivia.leave_management.entity.LeaveStatus;
import com.exprivia.leave_management.entity.LeaveType;
import com.exprivia.leave_management.exception.InsufficientBalanceException;
import com.exprivia.leave_management.exception.InvalidRequestException;
import com.exprivia.leave_management.exception.ResourceNotFoundException;
import com.exprivia.leave_management.exception.UnauthorizedException;
import com.exprivia.leave_management.repository.BalanceRepository;
import com.exprivia.leave_management.repository.EmployeeRepository;
import com.exprivia.leave_management.repository.HolidayRepository;
import com.exprivia.leave_management.repository.RequestRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveRequestService {
   @Autowired
   private RequestRepository requestRepository;
   @Autowired
   private BalanceRepository balanceRepository;
   @Autowired
   private EmployeeRepository employeeRepository;

   @Autowired
   private HolidayRepository holidayRepository;

   @Autowired
   private PasswordEncoder passwordEncoder;

   public List<LeaveRequestResponseDTO> getLeaveRequestsByEmployee(Long employeeId) {
      Employee employee = (Employee) this.employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Dipendente non trovato."));
      return this.requestRepository.findByEmployee(employee).stream().map(this::toDTO).toList();
   }

   @Transactional
   public LeaveRequestResponseDTO processLeaveRequest(Long leaveRequestId, LeaveStatus newLeaveStatus,
         String rejectionReason, Long managerId) {

      LeaveRequest leaveRequest = requestRepository.findById(leaveRequestId)
            .orElseThrow(() -> new ResourceNotFoundException("Richiesta non trovata."));

      Employee manager = leaveRequest.getEmployee().getManager();
      if (manager == null || !manager.getEmployeeId().equals(managerId)) {
         throw new UnauthorizedException("Non sei autorizzato a gestire questa richiesta.");
      }
      if (leaveRequest.getLeaveStatus() != LeaveStatus.PENDING) {
         throw new InvalidRequestException(
               "Impossibile modificare. La richiesta è già " + String.valueOf(leaveRequest.getLeaveStatus()));
      } else {
         if (newLeaveStatus == LeaveStatus.APPROVED) {
            LeaveBalance leaveBalance = (LeaveBalance) this.balanceRepository
                  .findByEmployeeAndReferenceYearAndLeaveType(leaveRequest.getEmployee(),
                        leaveRequest.getStartDate().getYear(), leaveRequest.getLeaveType())
                  .orElseThrow(() -> new ResourceNotFoundException("Saldo non trovato per l'aggiornamento."));
            if (leaveBalance.getUsedQuantity() == null) {
               leaveBalance.setUsedQuantity(BigDecimal.ZERO);
            }

            leaveBalance.setUsedQuantity(leaveBalance.getUsedQuantity().add(leaveRequest.getRequestedQuantity()));
            this.balanceRepository.save(leaveBalance);
         } else if (newLeaveStatus == LeaveStatus.REJECTED) {
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
               throw new InvalidRequestException("Errore: Inserire un motivo per il rifiuto.");
            }

            leaveRequest.setRejectionReason(rejectionReason);
         }

         leaveRequest.setLeaveStatus(newLeaveStatus);
         leaveRequest.setReadByEmployee(false);
         return this.toDTO((LeaveRequest) this.requestRepository.save(leaveRequest));
      }
   }

   @Transactional
   public LeaveRequestResponseDTO createDTOLeaveRequest(Long employeeId, LeaveRequestDTO dto) {
      if (dto.getStartDate() == null || dto.getStartDate().trim().isEmpty() ||
            dto.getEndDate() == null || dto.getEndDate().trim().isEmpty()) {
         throw new InvalidRequestException("I campi relativi alle date e agli orari sono obbligatori.");
      }
      Employee employee = (Employee) this.employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Dipendente non trovato."));
      LeaveRequest nuovaRichiesta = new LeaveRequest();
      nuovaRichiesta.setEmployee(employee);
      nuovaRichiesta.setLeaveType(LeaveType.valueOf(dto.getLeaveType()));
      nuovaRichiesta.setReason(dto.getReason());
      nuovaRichiesta.setLeaveStatus(LeaveStatus.PENDING);
      if (nuovaRichiesta.getLeaveType() == LeaveType.VACATION) {
         DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         nuovaRichiesta.setStartDate(LocalDate.parse(dto.getStartDate(), dateFormatter).atStartOfDay());
         nuovaRichiesta.setEndDate(LocalDate.parse(dto.getEndDate(), dateFormatter).atTime(23, 59));

         if (nuovaRichiesta.getEndDate().toLocalDate().isBefore(nuovaRichiesta.getStartDate().toLocalDate())) {
            throw new InvalidRequestException("La data di fine non può essere precedente alla data di inizio.");
         }

         if (nuovaRichiesta.getStartDate().toLocalDate().isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Non è possibile inserire richieste per date passate.");
         }

         Set<LocalDate> festivita = getFestivita();

         DayOfWeek startDay = nuovaRichiesta.getStartDate().getDayOfWeek();
         DayOfWeek endDay = nuovaRichiesta.getEndDate().getDayOfWeek();
         if (startDay == DayOfWeek.SATURDAY || startDay == DayOfWeek.SUNDAY
               || festivita.contains(nuovaRichiesta.getStartDate().toLocalDate())) {
            throw new InvalidRequestException(
                  "La data di inizio non può coincidere con un weekend o un giorno festivo.");
         }

         if (endDay == DayOfWeek.SATURDAY || endDay == DayOfWeek.SUNDAY
               || festivita.contains(nuovaRichiesta.getEndDate().toLocalDate())) {
            throw new InvalidRequestException("La data di fine non può coincidere con un weekend o un giorno festivo.");
         }

         long giorniLavorativi = 0L;

         for (LocalDateTime current = nuovaRichiesta.getStartDate(); !current.toLocalDate()
               .isAfter(nuovaRichiesta.getEndDate().toLocalDate()); current = current.plusDays(1L)) {
            DayOfWeek giorno = current.getDayOfWeek();
            boolean isWeekend = giorno == DayOfWeek.SATURDAY || giorno == DayOfWeek.SUNDAY;
            boolean isFesta = festivita.contains(current.toLocalDate());
            if (!isWeekend && !isFesta) {
               ++giorniLavorativi;
            }
         }

         if (giorniLavorativi == 0L) {
            throw new InvalidRequestException("Il periodo selezionato non contiene giorni lavorativi.");
         }

         nuovaRichiesta.setRequestedQuantity(BigDecimal.valueOf(giorniLavorativi));

      } else {
         DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
         nuovaRichiesta.setStartDate(LocalDateTime.parse(dto.getStartDate(), dateTimeFormatter));
         nuovaRichiesta.setEndDate(LocalDateTime.parse(dto.getEndDate(), dateTimeFormatter));

         if (nuovaRichiesta.getStartDate().isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("Non è possibile inserire richieste di permesso nel passato.");
         }

         Set<LocalDate> festivita = getFestivita();

         DayOfWeek day = nuovaRichiesta.getStartDate().getDayOfWeek();
         if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
               || festivita.contains(nuovaRichiesta.getStartDate().toLocalDate())) {
            throw new InvalidRequestException(
                  "Non è possibile richiedere un permesso durante un weekend o un giorno festivo.");
         }

         long minutiPermesso = ChronoUnit.MINUTES.between(nuovaRichiesta.getStartDate(), nuovaRichiesta.getEndDate());
         if (minutiPermesso <= 0L) {
            throw new InvalidRequestException("L'ora di fine deve essere successiva all'ora di inizio.");
         }

         double orePermesso = minutiPermesso / 60.0;
         if (orePermesso < 1.0) {
            throw new InvalidRequestException("I permessi devono essere di almeno 1 ora.");
         }
         nuovaRichiesta
               .setRequestedQuantity(BigDecimal.valueOf(orePermesso).setScale(2, java.math.RoundingMode.HALF_UP));

      }

      if (nuovaRichiesta.getStartDate().getYear() != nuovaRichiesta.getEndDate().getYear()) {
         throw new InvalidRequestException(
               "Non è possibile inserire una richiesta tra due anni. Inserire una richiesta fino al 31 Dicembre e una nuova dal 1 Gennaio.");
      } else {
         boolean isOverlapping = requestRepository.findByEmployee(employee).stream()
               .filter(r -> r.getLeaveStatus() != LeaveStatus.REJECTED
                     && r.getLeaveStatus() != LeaveStatus.CANCELED)
               .anyMatch(r -> !nuovaRichiesta.getStartDate().isAfter(r.getEndDate()) &&
                     !nuovaRichiesta.getEndDate().isBefore(r.getStartDate()));
         if (isOverlapping) {
            throw new InvalidRequestException(
                  "Esiste già una richiesta (in attesa o approvata) nel periodo selezionato.");
         } else {
            LeaveBalance leaveBalance = (LeaveBalance) this.balanceRepository
                  .findByEmployeeAndReferenceYearAndLeaveType(employee, nuovaRichiesta.getStartDate().getYear(),
                        nuovaRichiesta.getLeaveType())
                  .orElseThrow(() -> new ResourceNotFoundException(
                        "Saldo non trovato per l'anno " + nuovaRichiesta.getStartDate().getYear()));
            BigDecimal pendingQuantity = (BigDecimal) this.requestRepository.findByEmployee(employee).stream()
                  .filter((r) -> r.getLeaveStatus() == LeaveStatus.PENDING)
                  .filter((r) -> r.getLeaveType() == nuovaRichiesta.getLeaveType())
                  .filter((r) -> r.getStartDate().getYear() == nuovaRichiesta.getStartDate().getYear())
                  .map(LeaveRequest::getRequestedQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal virtualBalance = leaveBalance.getRemainingBalance().subtract(pendingQuantity);
            if (nuovaRichiesta.getRequestedQuantity().compareTo(virtualBalance) > 0) {
               String var10002 = leaveBalance.getRemainingBalance().stripTrailingZeros().toPlainString();
               String formattedPending = pendingQuantity.stripTrailingZeros().toPlainString();
               String unit = nuovaRichiesta.getLeaveType() == LeaveType.VACATION ? " giorni" : " ore";
               throw new InsufficientBalanceException("Saldo insufficiente. Saldo reale: " + var10002 + unit
                     + ". Quantità già bloccata in altre richieste in attesa: " + formattedPending + unit);
            } else {
               nuovaRichiesta.setCreationDate(LocalDateTime.now());
               return this.toDTO((LeaveRequest) this.requestRepository.save(nuovaRichiesta));
            }
         }
      }
   }

   private LeaveRequestResponseDTO toDTO(LeaveRequest lr) {
      LeaveRequestResponseDTO dto = new LeaveRequestResponseDTO();
      dto.setRequestId(lr.getRequestId());
      dto.setLeaveType(lr.getLeaveType().name());
      dto.setLeaveStatus(lr.getLeaveStatus().name());
      dto.setReason(lr.getReason());
      dto.setRejectionReason(lr.getRejectionReason());
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
      dto.setStartDate(lr.getStartDate().format(formatter));
      dto.setEndDate(lr.getEndDate().format(formatter));
      EmployeeResponseDTO empDto = new EmployeeResponseDTO();
      empDto.setEmployeeId(lr.getEmployee().getEmployeeId());
      empDto.setFirstName(lr.getEmployee().getFirstName());
      empDto.setLastName(lr.getEmployee().getLastName());
      empDto.setRole(lr.getEmployee().getRole().getName());
      dto.setEmployee(empDto);
      dto.setReadByManager(lr.getReadByManager() != null && lr.getReadByManager());
      dto.setReadByEmployee(lr.getReadByEmployee() != null && lr.getReadByEmployee());
      return dto;
   }

   public List<LeaveBalanceResponseDTO> getSaldoByEmployee(Long employeeId) {
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

   @Transactional
   public void cancelLeaveRequest(Long requestId, Long employeeId) {
      LeaveRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Richiesta non trovata."));

      if (!request.getEmployee().getEmployeeId().equals(employeeId)) {
         throw new UnauthorizedException("Non sei autorizzato a cancellare questa richiesta.");
      }

      if (request.getLeaveStatus() != LeaveStatus.PENDING) {
         throw new InvalidRequestException("Solo le richieste in attesa possono essere annullate.");
      }

      request.setLeaveStatus(LeaveStatus.CANCELED);
      request.setReadByEmployee(true);
      requestRepository.save(request);
   }

   public List<LeaveRequestResponseDTO> getRequestsForManager(Long managerId) {
      return this.requestRepository.findByEmployee_Manager_EmployeeId(managerId).stream().map(this::toDTO).toList();
   }

   @Transactional
   public void markAsRead(Long requestId, Long employeeId) {
      LeaveRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Richiesta non trovata."));

      if (!request.getEmployee().getEmployeeId().equals(employeeId)) {
         throw new UnauthorizedException("Non sei autorizzato a modificare questa richiesta.");
      }

      request.setReadByEmployee(true);
      requestRepository.save(request);
   }

   public void cambiaPassword(Long employeeId, String vecchiaPassword, String nuovaPassword) {
      Employee employee = (Employee) this.employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Dipendente non trovato"));
      if (!this.passwordEncoder.matches(vecchiaPassword, employee.getPassword())) {
         throw new InvalidRequestException("La password attuale non è corretta.");
      } else if (nuovaPassword != null && nuovaPassword.matches("^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$")) {
         employee.setPassword(this.passwordEncoder.encode(nuovaPassword));
         this.employeeRepository.save(employee);
      } else {
         throw new InvalidRequestException(
               "La password deve essere di almeno 8 caratteri e contenere almeno una maiuscola, un numero e un carattere speciale.");
      }
   }

   @Transactional
   public void markAsReadByManager(Long requestId, Long managerId) {
      LeaveRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Richiesta non trovata."));

      Employee manager = request.getEmployee().getManager();
      if (manager == null || !manager.getEmployeeId().equals(managerId)) {
         throw new UnauthorizedException("Non sei autorizzato a modificare questa richiesta.");
      }

      request.setReadByManager(true);
      requestRepository.save(request);
   }

   private Set<LocalDate> getFestivita() {
      return holidayRepository.findAll()
            .stream()
            .map(Holiday::getDate)
            .collect(java.util.stream.Collectors.toSet());
   }

   @Transactional
   public void impostaSaldo(Long employeeId, LeaveType tipo, int anno, BigDecimal quantita) {
      Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Dipendente non trovato."));

      LeaveBalance saldo = balanceRepository
            .findByEmployeeAndReferenceYearAndLeaveType(employee, anno, tipo)
            .orElseGet(() -> {
               LeaveBalance nuovo = new LeaveBalance();
               nuovo.setEmployee(employee);
               nuovo.setReferenceYear(anno);
               nuovo.setLeaveType(tipo);
               nuovo.setUsedQuantity(BigDecimal.ZERO);
               return nuovo;
            });

      saldo.setTotalQuantity(quantita);
      balanceRepository.save(saldo);
   }

   public List<AllBalanceResponseDTO> getTuttiSaldi() {
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
