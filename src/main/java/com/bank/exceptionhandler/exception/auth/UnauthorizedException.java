package com.bank.exceptionhandler.exception.auth;

// Thrown when a user is not authorized to perform an action (403)
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
