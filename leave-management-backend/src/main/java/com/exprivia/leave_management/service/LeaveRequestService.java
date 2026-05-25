package com.exprivia.leave_management.service;

import com.exprivia.leave_management.dto.EmployeeResponseDTO;
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
      LeaveRequest newRequest = new LeaveRequest();
      newRequest.setEmployee(employee);
      newRequest.setLeaveType(LeaveType.valueOf(dto.getLeaveType()));
      newRequest.setReason(dto.getReason());
      newRequest.setLeaveStatus(LeaveStatus.PENDING);
      if (newRequest.getLeaveType() == LeaveType.VACATION) {
         DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         newRequest.setStartDate(LocalDate.parse(dto.getStartDate(), dateFormatter).atStartOfDay());
         newRequest.setEndDate(LocalDate.parse(dto.getEndDate(), dateFormatter).atTime(23, 59));

         if (newRequest.getEndDate().toLocalDate().isBefore(newRequest.getStartDate().toLocalDate())) {
            throw new InvalidRequestException("La data di fine non può essere precedente alla data di inizio.");
         }

         if (newRequest.getStartDate().toLocalDate().isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Non è possibile inserire richieste per date passate.");
         }

         Set<LocalDate> holidays = getHolidays();

         DayOfWeek startDay = newRequest.getStartDate().getDayOfWeek();
         DayOfWeek endDay = newRequest.getEndDate().getDayOfWeek();
         if (startDay == DayOfWeek.SATURDAY || startDay == DayOfWeek.SUNDAY
               || holidays.contains(newRequest.getStartDate().toLocalDate())) {
            throw new InvalidRequestException(
                  "La data di inizio non può coincidere con un weekend o un giorno festivo.");
         }

         if (endDay == DayOfWeek.SATURDAY || endDay == DayOfWeek.SUNDAY
               || holidays.contains(newRequest.getEndDate().toLocalDate())) {
            throw new InvalidRequestException("La data di fine non può coincidere con un weekend o un giorno festivo.");
         }

         long workingDays = 0L;

         for (LocalDateTime current = newRequest.getStartDate(); !current.toLocalDate()
               .isAfter(newRequest.getEndDate().toLocalDate()); current = current.plusDays(1L)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
            boolean isHoliday = holidays.contains(current.toLocalDate());
            if (!isWeekend && !isHoliday) {
               ++workingDays;
            }
         }

         if (workingDays == 0L) {
            throw new InvalidRequestException("Il periodo selezionato non contiene giorni lavorativi.");
         }

         newRequest.setRequestedQuantity(BigDecimal.valueOf(workingDays));

      } else {
         DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
         newRequest.setStartDate(LocalDateTime.parse(dto.getStartDate(), dateTimeFormatter));
         newRequest.setEndDate(LocalDateTime.parse(dto.getEndDate(), dateTimeFormatter));

         if (newRequest.getStartDate().isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("Non è possibile inserire richieste di permesso nel passato.");
         }

         Set<LocalDate> holidays = getHolidays();

         DayOfWeek day = newRequest.getStartDate().getDayOfWeek();
         if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
               || holidays.contains(newRequest.getStartDate().toLocalDate())) {
            throw new InvalidRequestException(
                  "Non è possibile richiedere un permesso durante un weekend o un giorno festivo.");
         }

         long permitMinutes = ChronoUnit.MINUTES.between(newRequest.getStartDate(), newRequest.getEndDate());
         if (permitMinutes <= 0L) {
            throw new InvalidRequestException("L'ora di fine deve essere successiva all'ora di inizio.");
         }

         double permitHours = permitMinutes / 60.0;
         if (permitHours < 1.0) {
            throw new InvalidRequestException("I permessi devono essere di almeno 1 ora.");
         }
         newRequest
               .setRequestedQuantity(BigDecimal.valueOf(permitHours).setScale(2, java.math.RoundingMode.HALF_UP));

      }

      if (newRequest.getStartDate().getYear() != newRequest.getEndDate().getYear()) {
         throw new InvalidRequestException(
               "Non è possibile inserire una richiesta tra due anni. Inserire una richiesta fino al 31 Dicembre e una nuova dal 1 Gennaio.");
      } else {
         boolean isOverlapping = requestRepository.existsOverlapping(
               employee, newRequest.getStartDate(), newRequest.getEndDate());
         if (isOverlapping) {
            throw new InvalidRequestException(
                  "Esiste già una richiesta (in attesa o approvata) nel periodo selezionato.");
         } else {
            LeaveBalance leaveBalance = (LeaveBalance) this.balanceRepository
                  .findByEmployeeAndReferenceYearAndLeaveType(employee, newRequest.getStartDate().getYear(),
                        newRequest.getLeaveType())
                  .orElseThrow(() -> new ResourceNotFoundException(
                        "Saldo non trovato per l'anno " + newRequest.getStartDate().getYear()));
            BigDecimal pendingQuantity = (BigDecimal) this.requestRepository.findByEmployee(employee).stream()
                  .filter((r) -> r.getLeaveStatus() == LeaveStatus.PENDING)
                  .filter((r) -> r.getLeaveType() == newRequest.getLeaveType())
                  .filter((r) -> r.getStartDate().getYear() == newRequest.getStartDate().getYear())
                  .map(LeaveRequest::getRequestedQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal virtualBalance = leaveBalance.getRemainingBalance().subtract(pendingQuantity);
            if (newRequest.getRequestedQuantity().compareTo(virtualBalance) > 0) {
               String var10002 = leaveBalance.getRemainingBalance().stripTrailingZeros().toPlainString();
               String formattedPending = pendingQuantity.stripTrailingZeros().toPlainString();
               String unit = newRequest.getLeaveType() == LeaveType.VACATION ? " giorni" : " ore";
               throw new InsufficientBalanceException("Saldo insufficiente. Saldo reale: " + var10002 + unit
                     + ". Quantità già bloccata in altre richieste in attesa: " + formattedPending + unit);
            } else {
               newRequest.setCreationDate(LocalDateTime.now());
               return this.toDTO((LeaveRequest) this.requestRepository.save(newRequest));
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

   private Set<LocalDate> getHolidays() {
      return holidayRepository.findAll()
            .stream()
            .map(Holiday::getDate)
            .collect(java.util.stream.Collectors.toSet());
   }

}
