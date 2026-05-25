package com.exprivia.leave_management;

import com.exprivia.leave_management.dto.LeaveRequestDTO;
import com.exprivia.leave_management.entity.Employee;
import com.exprivia.leave_management.entity.LeaveBalance;
import com.exprivia.leave_management.entity.LeaveRequest;
import com.exprivia.leave_management.entity.LeaveStatus;
import com.exprivia.leave_management.exception.InsufficientBalanceException;
import com.exprivia.leave_management.exception.InvalidRequestException;
import com.exprivia.leave_management.repository.BalanceRepository;
import com.exprivia.leave_management.repository.EmployeeRepository;
import com.exprivia.leave_management.repository.HolidayRepository;
import com.exprivia.leave_management.repository.RequestRepository;
import com.exprivia.leave_management.service.LeaveRequestService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class LeaveRequestServiceTest {

    @Mock
    private RequestRepository requestRepository;
    @Mock
    private BalanceRepository balanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private HolidayRepository holidayRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    @Test
    void testCreateLeaveRequest_ThrowsException_IfCrossYear() {
        Long employeeId = 1L;
        Employee fintoDipendente = new Employee();
        fintoDipendente.setEmployeeId(employeeId);
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setLeaveType("VACATION");
        dto.setStartDate("2026-12-28");
        dto.setEndDate("2027-01-03");
        dto.setReason("Feste");
        Mockito.when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(fintoDipendente));
        InvalidRequestException exception = Assertions.assertThrows(InvalidRequestException.class,
                () -> leaveRequestService.createDTOLeaveRequest(employeeId, dto));
        Assertions.assertEquals(
                "Non è possibile inserire una richiesta tra due anni. Inserire una richiesta fino al 31 Dicembre e una nuova dal 1 Gennaio.",
                exception.getMessage());
    }

    @Test
    void testCreateLeaveRequest_ThrowsException_IfInsufficientBalance() {
        Long employeeId = 1L;
        Employee fintoDipendente = new Employee();
        fintoDipendente.setEmployeeId(employeeId);
        LeaveBalance fintoSaldo = new LeaveBalance();
        fintoSaldo.setTotalQuantity(BigDecimal.valueOf(5L));
        fintoSaldo.setUsedQuantity(BigDecimal.valueOf(3L));
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setLeaveType("VACATION");
        dto.setStartDate("2026-08-10");
        dto.setEndDate("2026-08-14");
        dto.setReason("Mare");
        Mockito.when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(fintoDipendente));
        Mockito.when(requestRepository.findByEmployee(fintoDipendente)).thenReturn(Collections.emptyList());
        Mockito.when(balanceRepository.findByEmployeeAndReferenceYearAndLeaveType(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(Optional.of(fintoSaldo));
        Mockito.when(holidayRepository.findAll()).thenReturn(Collections.emptyList());
        Assertions.assertThrows(InsufficientBalanceException.class,
                () -> leaveRequestService.createDTOLeaveRequest(employeeId, dto));
    }

    @Test
    void testCreateLeaveRequest_ThrowsException_IfNoWorkingDays() {
        Long employeeId = 1L;
        Employee fintoDipendente = new Employee();
        fintoDipendente.setEmployeeId(employeeId);
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setLeaveType("VACATION");
        dto.setStartDate("2026-06-06");
        dto.setEndDate("2026-06-07");
        dto.setReason("Weekend");
        Mockito.when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(fintoDipendente));
        Mockito.when(holidayRepository.findAll()).thenReturn(Collections.emptyList());
        Assertions.assertThrows(InvalidRequestException.class,
                () -> leaveRequestService.createDTOLeaveRequest(employeeId, dto));
    }

    @Test
    void testCreateLeaveRequest_ThrowsException_IfEndTimeBeforeStartTime() {
        Long employeeId = 1L;
        Employee fintoDipendente = new Employee();
        fintoDipendente.setEmployeeId(employeeId);
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setLeaveType("PERMIT");
        dto.setStartDate("2026-06-10 14:00");
        dto.setEndDate("2026-06-10 09:00");
        dto.setReason("Permesso");
        Mockito.when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(fintoDipendente));
        Assertions.assertThrows(InvalidRequestException.class,
                () -> leaveRequestService.createDTOLeaveRequest(employeeId, dto));
    }

    @Test
    void testProcessLeaveRequest_ThrowsException_IfAlreadyProcessed() {
        Long requestId = 1L;
        LeaveRequest fintoRequest = new LeaveRequest();
        fintoRequest.setRequestId(requestId);
        fintoRequest.setLeaveStatus(LeaveStatus.APPROVED);
        Mockito.when(requestRepository.findById(requestId)).thenReturn(Optional.of(fintoRequest));
        Assertions.assertThrows(InvalidRequestException.class,
                () -> leaveRequestService.processLeaveRequest(requestId, LeaveStatus.APPROVED, null, 99L));
    }
}