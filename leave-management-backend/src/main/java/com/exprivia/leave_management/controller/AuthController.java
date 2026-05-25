package com.exprivia.leave_management.controller;

import com.exprivia.leave_management.dto.LoginRequest;
import com.exprivia.leave_management.service.AuthService;
import jakarta.validation.Valid;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/auth"})
public class AuthController {
   private final AuthService authService;

   @PostMapping({"/login"})
   public ResponseEntity<String> login(@RequestBody @Valid LoginRequest loginRequest) {
      String jwtToken = this.authService.login(loginRequest);
      return ResponseEntity.ok(jwtToken);
   }

   @Generated
   public AuthController(final AuthService authService) {
      this.authService = authService;
   }
}
