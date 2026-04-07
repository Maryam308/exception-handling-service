package com.bank.exceptionhandler.model;

import lombok.Data;

@Data
public class TransferRequest {
    private double amount;
    private double balance;
}