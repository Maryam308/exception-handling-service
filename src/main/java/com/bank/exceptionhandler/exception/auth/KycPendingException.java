package com.bank.exceptionhandler.exception.auth;

public class KycPendingException extends RuntimeException {
    public KycPendingException(String message) {
        super(message);
    }
}
