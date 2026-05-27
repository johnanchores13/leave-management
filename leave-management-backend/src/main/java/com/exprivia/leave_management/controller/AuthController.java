package com.exprivia.leave_management.controller;

import com.exprivia.leave_management.dto.LoginRequest;
import com.exprivia.leave_management.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ "/api/auth" })
public class AuthController {

   @Autowired
   private AuthService authService;

   @PostMapping({ "/login" })
   public ResponseEntity<String> login(@RequestBody @Valid LoginRequest loginRequest) {
      String jwtToken = this.authService.login(loginRequest);
      return ResponseEntity.ok(jwtToken);
   }

}
