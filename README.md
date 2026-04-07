# Centralized Exception Handling Service

---

## What is this?

This is a standalone Spring Boot service that demonstrates **centralized exception handling**, a reusable pattern where all error responses across the entire system are handled in one place, consistently.

**Without this pattern:**
- Every microservice writes its own error handling logic
- Errors come back in different formats (stack traces, HTML pages, inconsistent JSON)
- Frontend developers can't predict what an error response looks like

**With this pattern:**
- Every error, from any microservice, returns the same clean JSON structure
- Errors are organized by domain (auth, transfer, credit, admin, account)
- One file to update if the format ever needs to change
- Professional, predictable, production-ready API behavior

---

## Tech Stack

- Java 21
- Spring Boot 
- Maven

---

## Project Structure

```
src/main/java/com/bank/exceptionhandler/
│
├── ExceptionHandlingServiceApplication.java
│
├── controller/
│   └── DemoController.java              # Demo endpoints to trigger each exception
│
├── exception/
│   ├── GlobalExceptionHandler.java      ⭐ THE CORE: handles all exceptions in one place
│   ├── ResourceNotFoundException.java   # 404: resource not found
│   ├── BadRequestException.java         # 400: invalid request
│   │
│   ├── auth/
│   │   ├── UnauthorizedException.java   # 403: no permission
│   │   └── KycPendingException.java     # 403: KYC not completed
│   │
│   ├── transfer/
│   │   ├── InsufficientFundsException.java    # 400: not enough balance
│   │   └── FxRateUnavailableException.java    # 503: exchange rate service down
│   │
│   ├── credit/
│   │   └── CreditLimitExceededException.java  # 400: over credit limit
│   │
│   ├── admin/
│   │   └── AccountBlockedException.java       # 403: account is blocked
│   │
│   └── account/                               # Reserved for future account-related exceptions
│
├── model/
│   ├── ErrorResponse.java               # The consistent JSON error structure
│   ├── RegisterRequest.java             # Demo request with validation annotations
│   └── TransferRequest.java             # Demo transfer request body
│
└── resources/
    └── application.properties
```

---

## Error Response Format

Every error in the system returns this exact structure:

```json
{
  "status": 403,
  "errorCode": "AUTH-050",
  "error": "KYC_PENDING",
  "message": "Identity verification pending. Please complete KYC to access banking features.",
  "timestamp": "2026-04-07T10:30:00"
}
```

For validation errors, a `fieldErrors` array is also included:

```json
{
  "status": 400,
  "errorCode": "VAL-400",
  "error": "VALIDATION_FAILED",
  "message": "One or more fields are invalid",
  "timestamp": "2026-04-07T10:30:00",
  "fieldErrors": [
    { "field": "email", "message": "Email must be a valid email address" },
    { "field": "password", "message": "Password must be at least 6 characters" }
  ]
}
```

---

## Error Code Reference

| Error Code   | Exception                    | HTTP Status | Scenario                          |
|--------------|------------------------------|-------------|-----------------------------------|
| REQ-404      | ResourceNotFoundException    | 404         | User, account, or resource not found |
| REQ-400      | BadRequestException          | 400         | Invalid request or operation      |
| AUTH-401     | UnauthorizedException        | 403         | Insufficient permissions          |
| AUTH-050     | KycPendingException          | 403         | KYC not completed                 |
| TRANSFER-402 | InsufficientFundsException   | 400         | Not enough balance                |
| FX-101       | FxRateUnavailableException   | 503         | Exchange rate service unavailable |
| CRED-401     | CreditLimitExceededException | 400         | Over credit card limit            |
| ADM-403      | AccountBlockedException      | 403         | Account blocked by admin          |
| VAL-400      | MethodArgumentNotValidException | 400      | Invalid or missing fields         |
| SYS-500      | Exception (catch-all)        | 500         | Unexpected server error           |

---

## How to Run

1. Open IntelliJ → **File → Open** → select the `exception-handling-service` folder
2. Wait for Maven to import dependencies
3. Run `ExceptionHandlingServiceApplication.java`
4. Service starts on **http://localhost:8080**

---

## Testing with Postman

Import the collection from the `postman/` folder:

```
postman/Digital_Bank_-_Exception_Handling_POC_postman_collection.json
```

The collection includes 11 pre-built requests covering all error scenarios:

| # | Request | Expected Response |
|---|---------|-------------------|
| 1 | GET /api/demo/users/999 | 404 RESOURCE_NOT_FOUND |
| 2 | GET /api/demo/admin?role=USER | 403 UNAUTHORIZED |
| 3 | GET /api/demo/kyc/check | 403 KYC_PENDING |
| 4 | GET /api/demo/account/login | 403 ACCOUNT_BLOCKED |
| 5 | POST /api/demo/transfer `{"amount":9999,"balance":100}` | 400 INSUFFICIENT_FUNDS |
| 6 | GET /api/demo/fx/rate | 503 RATE_UNAVAILABLE |
| 7 | POST /api/demo/credit/spend?amount=6000 | 400 CREDIT_LIMIT_EXCEEDED |
| 8 | GET /api/demo/account?status=CLOSED | 400 BAD_REQUEST |
| 9 | POST /api/demo/register `{"name":"","email":"bad","password":"1"}` | 400 VALIDATION_FAILED |
| 10 | GET /api/demo/crash | 500 INTERNAL_SERVER_ERROR |
| 11 | GET /api/demo/users/123 | 200 Success |

---

## How Other Teams Integrate This

Copy the `exception/` folder into any microservice. Then just throw exceptions anywhere — no try/catch needed:

```java
// In Auth Service:
throw new KycPendingException("Please complete your KYC before proceeding");

// In Transfer Service:
throw new InsufficientFundsException("Available: 50 BHD, Requested: 200 BHD");

// In Credit Service:
throw new CreditLimitExceededException("Limit: 5000 BHD, Requested: 7000 BHD");

// In Admin Service:
throw new AccountBlockedException("Account suspended due to suspicious activity");
```

The `GlobalExceptionHandler` catches everything automatically and returns the correct HTTP status and error code.

---

## Extending This Service

New exceptions can be added at any time as the system grows. Simply create a new exception class in the relevant domain folder:

- `exception/auth/` → authentication & KYC related errors
- `exception/transfer/` → payment & FX related errors
- `exception/credit/` → credit card related errors
- `exception/admin/` → admin actions & account management
- `exception/account/` → account operations & restrictions

Then register a handler for it in `GlobalExceptionHandler.java`. No other files need to change.

