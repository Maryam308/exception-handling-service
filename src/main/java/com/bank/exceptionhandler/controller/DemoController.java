package com.bank.exceptionhandler.controller;

import com.bank.exceptionhandler.exception.BadRequestException;
import com.bank.exceptionhandler.exception.admin.AccountBlockedException;
import com.bank.exceptionhandler.exception.auth.KycPendingException;
import com.bank.exceptionhandler.exception.credit.CreditLimitExceededException;
import com.bank.exceptionhandler.exception.transfer.FxRateUnavailableException;
import com.bank.exceptionhandler.exception.transfer.InsufficientFundsException;
import com.bank.exceptionhandler.exception.ResourceNotFoundException;
import com.bank.exceptionhandler.exception.auth.UnauthorizedException;
import com.bank.exceptionhandler.model.TransferRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bank.exceptionhandler.model.RegisterRequest;

/**
 * DemoController
 *
 * This controller exists purely to demonstrate how the GlobalExceptionHandler works.
 * Each endpoint simulates a real banking scenario and throws the appropriate exception.
 *
 * In the real system, other microservices would throw these same exceptions
 * from their own controllers/services, the GlobalExceptionHandler catches all of them.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    // Simulates: fetching a user that doesn't exist
    // GET /api/demo/users/999
    @GetMapping("/users/{id}")
    public ResponseEntity<String> getUser(@PathVariable Long id) {
        if (id == 999) {
            throw new ResourceNotFoundException("User with ID " + id + " not found");
        }
        return ResponseEntity.ok("User " + id + " found successfully");
    }

    // Simulates: trying to access admin panel without permission
    // GET /api/demo/admin?role=USER
    @GetMapping("/admin")
    public ResponseEntity<String> accessAdmin(@RequestParam String role) {
        if (!role.equals("ADMIN")) {
            throw new UnauthorizedException("Access denied. Admin role required.");
        }
        return ResponseEntity.ok("Welcome to the admin panel");
    }

    // Simulates: transfer with insufficient funds
    // POST /api/demo/transfer with { "amount": 9999, "balance": 100 }
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
        if (request.getAmount() > request.getBalance()) {
            throw new InsufficientFundsException(
                    "Insufficient funds. Available: " + request.getBalance() +
                            " BHD, Requested: " + request.getAmount() + " BHD"
            );
        }
        return ResponseEntity.ok("Transfer of " + request.getAmount() + " BHD completed successfully");
    }

    // Simulates: submitting invalid data (validation)
    //POST /api/demo/register with missing or invalid fields
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok("User " + request.getName() + " registered successfully");
    }

    // Simulates: a bad request with custom message
    //GET /api/demo/account?status=CLOSED
    @GetMapping("/account")
    public ResponseEntity<String> getAccount(@RequestParam String status) {
        if (status.equals("CLOSED")) {
            throw new BadRequestException("Cannot perform operations on a closed account");
        }
        return ResponseEntity.ok("Account is active");
    }

    // Simulates: an unexpected server error
    //GET /api/demo/crash
    @GetMapping("/crash")
    public ResponseEntity<String> crash() {
        throw new RuntimeException("Something went terribly wrong internally");
    }

    // Tests KycPendingException - GET /api/demo/kyc/check
    @GetMapping("/kyc/check")
    public ResponseEntity<String> checkKyc() {
        throw new KycPendingException("KYC verification pending. Please upload your documents.");
    }

    // Tests FxRateUnavailableException - GET /api/demo/fx/rate
    @GetMapping("/fx/rate")
    public ResponseEntity<String> getFxRate() {
        throw new FxRateUnavailableException("Exchange rate service temporarily unavailable. Please try again later.");
    }

    // Tests CreditLimitExceededException - POST /api/demo/credit/spend?amount=6000
    @PostMapping("/credit/spend")
    public ResponseEntity<String> creditSpend(@RequestParam double amount) {
        double creditLimit = 5000;
        if (amount > creditLimit) {
            throw new CreditLimitExceededException(
                    "Credit limit exceeded. Limit: " + creditLimit + " BHD, Requested: " + amount + " BHD"
            );
        }
        return ResponseEntity.ok("Spent " + amount + " BHD using credit card");
    }

    @GetMapping("/account/login")
    public ResponseEntity<String> accountLogin() {
        throw new AccountBlockedException("Your account has been blocked. Please contact support.");
    }
}