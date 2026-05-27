package com.exprivia.leave_management.service;

import com.exprivia.leave_management.dto.AdminEmployeeDTO;
import com.exprivia.leave_management.dto.UpdateEmployeeDTO;
import com.exprivia.leave_management.entity.Department;
import com.exprivia.leave_management.entity.Employee;
import com.exprivia.leave_management.exception.InvalidRequestException;
import com.exprivia.leave_management.exception.ResourceNotFoundException;
import com.exprivia.leave_management.repository.BalanceRepository;
import com.exprivia.leave_management.repository.DepartmentRepository;
import com.exprivia.leave_management.repository.EmployeeRepository;
import com.exprivia.leave_management.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private RequestRepository requestRepository;

    public Page<AdminEmployeeDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(e -> {
            AdminEmployeeDTO dto = new AdminEmployeeDTO();
            dto.setEmployeeId(e.getEmployeeId());
            dto.setSerialNumber(e.getSerialNumber());
            dto.setFirstName(e.getFirstName());
            dto.setLastName(e.getLastName());
            dto.setEmail(e.getEmail());
            dto.setHiringDate(e.getHiringDate());
            dto.setDepartmentName(e.getDepartment() != null ? e.getDepartment().getName() : "—");
            dto.setRoleName(e.getRole() != null ? e.getRole().getName() : "—");
            dto.setManagerFirstName(e.getManager() != null ? e.getManager().getFirstName() : "—");
            dto.setManagerLastName(e.getManager() != null ? e.getManager().getLastName() : "");
            dto.setDepartmentId(e.getDepartment() != null ? e.getDepartment().getDepartmentId() : null);
            dto.setManagerId(e.getManager() != null ? e.getManager().getEmployeeId() : null);
            return dto;
        });
    }

    public List<AdminEmployeeDTO> getManagers() {
        return employeeRepository.findByRole_Name("RESPONSABILE").stream()
                .map(e -> {
                    AdminEmployeeDTO dto = new AdminEmployeeDTO();
                    dto.setEmployeeId(e.getEmployeeId());
                    dto.setFirstName(e.getFirstName());
                    dto.setLastName(e.getLastName());
                    return dto;
                }).toList();
    }

    @Transactional
    public void updateEmployee(Long employeeId, UpdateEmployeeDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dipendente non trovato."));

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reparto non trovato."));
            employee.setDepartment(department);
        }

        if (dto.getManagerId() != null) {
            if (dto.getManagerId().equals(employeeId)) {
                throw new InvalidRequestException("Un dipendente non può essere responsabile di sé stesso.");
            }
            Employee manager = employeeRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Responsabile non trovato."));
            employee.setManager(manager);
        }

        employeeRepository.save(employee);
    }

    @Transactional
    public void deleteEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dipendente non trovato."));

        balanceRepository.deleteAll(balanceRepository.findByEmployee(employee));
        requestRepository.deleteAll(requestRepository.findByEmployee(employee));
        employeeRepository.delete(employee);
    }

    @Transactional
    public void createDepartment(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidRequestException("Il nome del reparto è obbligatorio.");
        }
        Department department = new Department();
        department.setName(name);
        departmentRepository.save(department);
    }

}
