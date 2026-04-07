package com.bank.exceptionhandler.exception.transfer;

// Thrown when a user doesn't have enough balance for a transaction (400)
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}