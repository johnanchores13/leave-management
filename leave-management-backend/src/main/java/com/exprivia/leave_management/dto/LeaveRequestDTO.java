package com.exprivia.leave_management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LeaveRequestDTO {
   private @NotBlank(message = "Il tipo di permesso è obbligatorio.") String leaveType;
   private @NotBlank(message = "La data di inizio è obbligatoria.") String startDate;
   private @NotBlank(message = "La data di fine è obbligatoria.") String endDate;
   private @NotBlank(message = "Il motivo è obbligatorio.") String reason;

}
