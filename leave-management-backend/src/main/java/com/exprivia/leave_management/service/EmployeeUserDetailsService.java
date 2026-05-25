package com.exprivia.leave_management.service;

import com.exprivia.leave_management.entity.Employee;
import com.exprivia.leave_management.repository.EmployeeRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmployeeUserDetailsService implements UserDetailsService {
   @Autowired
   private EmployeeRepository employeeRepository;

   public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
      Employee employee = (Employee)this.employeeRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Dipendente non trovato: " + email));
      return new User(employee.getEmail(), employee.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole().getName())));
   }
}
