package com.exprivia.leave_management.exception;

public class InvalidRequestException extends RuntimeException {
   public InvalidRequestException(String message) {
      super(message);
   }
}
