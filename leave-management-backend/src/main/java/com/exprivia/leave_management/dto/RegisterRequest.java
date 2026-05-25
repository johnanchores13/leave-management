package com.exprivia.leave_management.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
   private @NotBlank(message = "Il nome è obbligatorio.") String firstName;
   private @NotBlank(message = "Il cognome è obbligatorio.") String lastName;
   private @NotBlank(message = "La matricola è obbligatoria.") String serialNumber;
   private @NotBlank(message = "L'email è obbligatoria.") @Email(message = "Formato email non valido.") String email;
   private @NotBlank(message = "La password è obbligatoria.") @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$", message = "La password deve essere di almeno 8 caratteri e contenere almeno una maiuscola, un numero e un carattere speciale.") String password;
   private @NotNull(message = "La data di assunzione è obbligatoria.") LocalDate hiringDate;
   private Long departmentId;
   private Long managerId;
   private String role;
}
