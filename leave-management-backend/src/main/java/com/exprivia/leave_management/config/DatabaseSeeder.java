package com.exprivia.leave_management.config;

import com.exprivia.leave_management.entity.Department;
import com.exprivia.leave_management.entity.Employee;
import com.exprivia.leave_management.entity.LeaveBalance;
import com.exprivia.leave_management.entity.LeaveType;
import com.exprivia.leave_management.entity.Role;
import com.exprivia.leave_management.repository.BalanceRepository;
import com.exprivia.leave_management.repository.DepartmentRepository;
import com.exprivia.leave_management.repository.EmployeeRepository;
import com.exprivia.leave_management.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository, EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder, BalanceRepository balanceRepository,
            DepartmentRepository departmentRepository) {
        return args -> {
            if (roleRepository.count() == 0) {
                Role adminRole = new Role();
                adminRole.setName("ADMIN");

                Role respRole = new Role();
                respRole.setName("RESPONSABILE");

                Role dipRole = new Role();
                dipRole.setName("DIPENDENTE");

                roleRepository.saveAll(List.of(adminRole, respRole, dipRole));
            }

            if (employeeRepository.count() == 0) {
                Department itDept = new Department();
                itDept.setName("IT & Sviluppo");
                Department comDept = new Department();
                comDept.setName("Commerciale");

                if (departmentRepository.count() == 0) {
                    departmentRepository.saveAll(List.of(itDept, comDept));
                } else {
                    List<Department> depts = departmentRepository.findAll();
                    itDept = depts.size() > 0 ? depts.get(0) : itDept;
                    comDept = depts.size() > 1 ? depts.get(1) : comDept;
                }

                Role adminRole = roleRepository.findByName("ADMIN").get();
                Role respRole = roleRepository.findByName("RESPONSABILE").get();
                Role dipRole = roleRepository.findByName("DIPENDENTE").get();

                String commonPassword = passwordEncoder.encode("Password123!");

                Employee admin = new Employee();
                admin.setFirstName("Admin");
                admin.setLastName("Sistema");
                admin.setEmail("admin@exprivia.it");
                admin.setSerialNumber("A001");
                admin.setPassword(commonPassword);
                admin.setRole(adminRole);
                admin.setHiringDate(LocalDate.of(2020, 1, 1));
                employeeRepository.save(admin);

                Employee manager1 = new Employee();
                manager1.setFirstName("Mario");
                manager1.setLastName("Rossi");
                manager1.setEmail("mario.rossi@exprivia.it");
                manager1.setSerialNumber("R001");
                manager1.setPassword(commonPassword);
                manager1.setRole(respRole);
                manager1.setDepartment(itDept);
                manager1.setHiringDate(LocalDate.of(2015, 6, 1));
                employeeRepository.save(manager1);

                Employee manager2 = new Employee();
                manager2.setFirstName("Luigi");
                manager2.setLastName("Verdi");
                manager2.setEmail("luigi.verdi@exprivia.it");
                manager2.setSerialNumber("R002");
                manager2.setPassword(commonPassword);
                manager2.setRole(respRole);
                manager2.setDepartment(comDept);
                manager2.setHiringDate(LocalDate.of(2018, 3, 15));
                employeeRepository.save(manager2);

                Employee emp1 = new Employee();
                emp1.setFirstName("Giulia");
                emp1.setLastName("Bianchi");
                emp1.setEmail("giulia.bianchi@exprivia.it");
                emp1.setSerialNumber("D001");
                emp1.setPassword(commonPassword);
                emp1.setRole(dipRole);
                emp1.setManager(manager1);
                emp1.setDepartment(itDept);
                emp1.setHiringDate(LocalDate.of(2022, 1, 10));
                employeeRepository.save(emp1);

                Employee emp2 = new Employee();
                emp2.setFirstName("Luca");
                emp2.setLastName("Neri");
                emp2.setEmail("luca.neri@exprivia.it");
                emp2.setSerialNumber("D002");
                emp2.setPassword(commonPassword);
                emp2.setRole(dipRole);
                emp2.setManager(manager2);
                emp2.setDepartment(comDept);
                emp2.setHiringDate(LocalDate.of(2023, 9, 1));
                employeeRepository.save(emp2);

                Employee emp3 = new Employee();
                emp3.setFirstName("Marco");
                emp3.setLastName("Gialli");
                emp3.setEmail("marco.gialli@exprivia.it");
                emp3.setSerialNumber("D003");
                emp3.setPassword(commonPassword);
                emp3.setRole(dipRole);
                emp3.setManager(manager1);
                emp3.setDepartment(itDept);
                emp3.setHiringDate(LocalDate.of(2023, 11, 15));
                employeeRepository.save(emp3);

                Employee emp4 = new Employee();
                emp4.setFirstName("Elena");
                emp4.setLastName("Viola");
                emp4.setEmail("elena.viola@exprivia.it");
                emp4.setSerialNumber("D004");
                emp4.setPassword(commonPassword);
                emp4.setRole(dipRole);
                emp4.setManager(manager2);
                emp4.setDepartment(comDept);
                emp4.setHiringDate(LocalDate.of(2024, 2, 1));
                employeeRepository.save(emp4);

                int year = LocalDate.now().getYear();
                for (Employee e : Arrays.asList(manager1, manager2, emp1, emp2, emp3, emp4)) {
                    LeaveBalance vacation = new LeaveBalance();
                    vacation.setEmployee(e);
                    vacation.setLeaveType(LeaveType.VACATION);
                    vacation.setReferenceYear(year);
                    vacation.setTotalQuantity(BigDecimal.valueOf(20));
                    vacation.setUsedQuantity(BigDecimal.ZERO);

                    LeaveBalance permits = new LeaveBalance();
                    permits.setEmployee(e);
                    permits.setLeaveType(LeaveType.PERMIT);
                    permits.setReferenceYear(year);
                    permits.setTotalQuantity(BigDecimal.valueOf(48));
                    permits.setUsedQuantity(BigDecimal.ZERO);

                    balanceRepository.saveAll(Arrays.asList(vacation, permits));
                }
            }
        };
    }
}
