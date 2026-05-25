package com.exprivia.leave_management.exception;

public class UnauthorizedException extends RuntimeException {
   public UnauthorizedException(String message) {
      super(message);
   }
}
