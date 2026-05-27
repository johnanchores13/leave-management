package com.exprivia.leave_management.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordDTO {

    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$", message = "La password deve essere di almeno 8 caratteri, con maiuscola, numero e carattere speciale.")
    private String newPassword;

    private String oldPassword;

}
