package com.bank.exceptionhandler.exception;

import com.bank.exceptionhandler.exception.admin.AccountBlockedException;
import com.bank.exceptionhandler.exception.auth.KycPendingException;
import com.bank.exceptionhandler.exception.auth.UnauthorizedException;
import com.bank.exceptionhandler.exception.credit.CreditLimitExceededException;
import com.bank.exceptionhandler.exception.transfer.FxRateUnavailableException;
import com.bank.exceptionhandler.exception.transfer.InsufficientFundsException;
import com.bank.exceptionhandler.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 - Resource Not Found (Generic)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "REQ-404", "RESOURCE_NOT_FOUND", ex.getMessage());
    }

    // 400 - Bad Request (Generic)
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "REQ-400", "BAD_REQUEST", ex.getMessage());
    }

    // 403 - Unauthorized (Auth)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "AUTH-401", "UNAUTHORIZED", ex.getMessage());
    }

    // 403 - KYC Pending (Auth)
    @ExceptionHandler(KycPendingException.class)
    public ResponseEntity<ErrorResponse> handleKycPending(KycPendingException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "AUTH-050", "KYC_PENDING",
                "Identity verification pending. Please complete KYC to access banking features.");
    }

    // 400 - Insufficient Funds (Transfer)
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "TRANSFER-402", "INSUFFICIENT_FUNDS", ex.getMessage());
    }

    // 503 - FX Rate Unavailable (Transfer)
    @ExceptionHandler(FxRateUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleFxRateUnavailable(FxRateUnavailableException ex) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "FX-101", "RATE_UNAVAILABLE",
                "Exchange rates temporarily unavailable. Please try again later.");
    }

    // 400 - Credit Limit Exceeded (Credit)
    @ExceptionHandler(CreditLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleCreditLimitExceeded(CreditLimitExceededException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "CRED-401", "CREDIT_LIMIT_EXCEEDED", ex.getMessage());
    }

    // 403 - Account Blocked (Admin)
    @ExceptionHandler(AccountBlockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountBlocked(AccountBlockedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "ADM-403", "ACCOUNT_BLOCKED",
                "Your account has been blocked. Please contact support.");
    }

    // 400 - Validation errors (Spring built-in)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .toList();

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("VAL-400")
                .error("VALIDATION_FAILED")
                .message("One or more fields are invalid")
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 500 - Catch-all for any unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Log stack trace for debugging
        log.error("Unexpected error occurred", ex);

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SYS-500", "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.");
    }

    // Helper method to build consistent error responses
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String errorCode, String error, String message) {
        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .errorCode(errorCode)
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(response);
    }
}