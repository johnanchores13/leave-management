package com.exprivia.leave_management.exception;

public class InsufficientBalanceException extends RuntimeException {
   public InsufficientBalanceException(String message) {
      super(message);
   }
}
