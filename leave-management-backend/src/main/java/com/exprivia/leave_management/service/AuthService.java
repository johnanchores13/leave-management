package com.exprivia.leave_management.service;

import com.exprivia.leave_management.config.JwtUtil;
import com.exprivia.leave_management.dto.LoginRequest;
import com.exprivia.leave_management.dto.RegisterRequest;
import com.exprivia.leave_management.entity.Department;
import com.exprivia.leave_management.entity.Employee;
import com.exprivia.leave_management.entity.Role;
import com.exprivia.leave_management.exception.InvalidRequestException;
import com.exprivia.leave_management.exception.ResourceNotFoundException;
import com.exprivia.leave_management.exception.UnauthorizedException;
import com.exprivia.leave_management.repository.DepartmentRepository;
import com.exprivia.leave_management.repository.EmployeeRepository;
import com.exprivia.leave_management.repository.RoleRepository;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public String login(LoginRequest request) {
        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email o password non valida."));

        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new UnauthorizedException("Email o password non valida.");
        }

        return jwtUtil.generateToken(
                employee.getEmployeeId(),
                employee.getEmail(),
                employee.getRole().getName());
    }

    public void register(RegisterRequest request) {
        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new InvalidRequestException("Email già registrata.");
        }

        String roleName = (request.getRole() != null && !request.getRole().isBlank())
                ? request.getRole()
                : "DIPENDENTE";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Ruolo '" + roleName + "' non trovato nel database."));
        Employee employee = new Employee();
        employee.setEmail(request.getEmail());
        employee.setPassword(passwordEncoder.encode(request.getPassword()));
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setSerialNumber(request.getSerialNumber());
        employee.setRole(role);
        employee.setHiringDate(request.getHiringDate() != null ? request.getHiringDate() : LocalDate.now());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reparto non trovato."));
            employee.setDepartment(dept);
        }

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Responsabile non trovato."));
            employee.setManager(manager);
        }

        employeeRepository.save(employee);
    }

    public void createEmployee(RegisterRequest request) {
        this.register(request);
    }

    public void changePassword(Long employeeId, String oldPassword, String newPassword) {
        Employee employee = (Employee) this.employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dipendente non trovato"));
        if (!this.passwordEncoder.matches(oldPassword, employee.getPassword())) {
            throw new InvalidRequestException("La password attuale non è corretta.");
        } else if (newPassword != null) {
            employee.setPassword(this.passwordEncoder.encode(newPassword));
            this.employeeRepository.save(employee);
        } else {
            throw new InvalidRequestException(
                    "La password deve essere di almeno 8 caratteri e contenere almeno una maiuscola, un numero e un carattere speciale.");
        }
    }
}
