package com.exprivia.leave_management.service;

import com.exprivia.leave_management.dto.AdminEmployeeDTO;
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
import java.util.Map;

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
        return employeeRepository.findAll().stream()
                .filter(e -> e.getRole() != null && "RESPONSABILE".equals(e.getRole().getName()))
                .map(e -> {
                    AdminEmployeeDTO dto = new AdminEmployeeDTO();
                    dto.setEmployeeId(e.getEmployeeId());
                    dto.setFirstName(e.getFirstName());
                    dto.setLastName(e.getLastName());
                    return dto;
                }).toList();
    }

    @Transactional
    public void updateEmployee(Long employeeId, Map<String, Object> body) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dipendente non trovato."));

        if (body.containsKey("departmentId")) {
            Long departmentId = Long.valueOf(body.get("departmentId").toString());
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Reparto non trovato."));
            employee.setDepartment(department);
        }

        if (body.containsKey("managerId")) {
            Object managerIdObj = body.get("managerId");
            if (managerIdObj == null) {
                employee.setManager(null);
            } else {
                Long managerId = Long.valueOf(managerIdObj.toString());
                if (managerId.equals(employeeId)) {
                    throw new InvalidRequestException("Un dipendente non può essere responsabile di sé stesso.");
                }
                Employee manager = employeeRepository.findById(managerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Responsabile non trovato."));
                employee.setManager(manager);
            }
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
    public void createDepartment(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new InvalidRequestException("Il nome del reparto è obbligatorio.");
        }
        Department department = new Department();
        department.setName(nome);
        departmentRepository.save(department);
    }
}
