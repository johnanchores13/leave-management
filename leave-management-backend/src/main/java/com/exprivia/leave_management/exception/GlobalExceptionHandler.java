package com.exprivia.leave_management.exception;

import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
   @ExceptionHandler({ ResourceNotFoundException.class })
   public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
      ErrorResponse error = new ErrorResponse(404, "Not Found", ex.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
   }

   @ExceptionHandler({ InsufficientBalanceException.class })
   public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex) {
      ErrorResponse error = new ErrorResponse(422, "Unprocessable Entity", ex.getMessage());
      return ResponseEntity.status(422).body(error);
   }

   @ExceptionHandler({ InvalidRequestException.class })
   public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException ex) {
      ErrorResponse error = new ErrorResponse(400, "Bad Request", ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
   }

   @ExceptionHandler({ Exception.class })
   public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
      log.error("Errore interno del server", ex);
      ErrorResponse error = new ErrorResponse(500, "Internal Server Error", "Si è verificato un errore interno.");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
   }

   @ExceptionHandler({ UnauthorizedException.class })
   public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
      ErrorResponse error = new ErrorResponse(401, "Unauthorized", ex.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
   }

   @ExceptionHandler({ MethodArgumentNotValidException.class })
   public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
      String message = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> "• " + err.getDefaultMessage())
            .collect(Collectors.joining("\n"));

      ErrorResponse error = new ErrorResponse(400, "Bad Request", message);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
   }

   @ExceptionHandler({ AccessDeniedException.class })
   public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
      ErrorResponse error = new ErrorResponse(403, "Forbidden", ex.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
   }
}
