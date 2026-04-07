package com.bank.exceptionhandler.exception.transfer;

public class FxRateUnavailableException extends RuntimeException {
    public FxRateUnavailableException(String message) {
        super(message);
    }
}