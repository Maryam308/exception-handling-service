package com.bank.exceptionhandler.exception;

// Thrown when the request is invalid or missing required data (400)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
