# FinSecure API Documentation

Base URL: `https://localhost:8080/api`

All authenticated endpoints require JWT token in Authorization header:
```
Authorization: Bearer <access_token>
```

## Authentication Endpoints

### 1. Register Customer

**POST** `/auth/register`

Register a new customer account.

**Request Body:**
```json
{
  "email": "customer@example.com",
  "password": "SecurePass@123",
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1995-01-15",
  "phone": "+919876543210",
  "address": "123 Main Street",
  "city": "Mumbai",
  "state": "Maharashtra",
  "postalCode": "400001",
  "accountType": "ADULT",
  "guardianCustomerId": null
}
```

**Account Types:**
- `STUDENT` - For college students
- `ADULT` - For adult customers (18+)
- `MINOR` - For minors (requires guardianCustomerId)

**Response:**
```json
{
  "success": true,
  "message": "Registration successful. Please complete KYC verification.",
  "data": 12345,
  "error": null
}
```

### 2. Login

**POST** `/auth/login`

Login with email and password. Returns success message and triggers OTP email.

**Request Body:**
```json
{
  "email": "customer@example.com",
  "password": "SecurePass@123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP sent to your email. Please verify to complete login.",
  "data": null,
  "error": null
}
```

### 3. Verify OTP

**POST** `/auth/verify-otp`

Verify OTP and complete login. Returns JWT tokens.

**Request Body:**
```json
{
  "email": "customer@example.com",
  "otpCode": "123456",
  "otpType": "LOGIN"
}
```

**OTP Types:**
- `LOGIN` - Login verification
- `TRANSACTION` - Transaction verification
- `CARD_BLOCK` - Card blocking/replacement
- `PASSWORD_RESET` - Password reset
- `PROFILE_UPDATE` - Profile changes
- `KYC_CONFIRMATION` - Employee KYC actions

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "customer@example.com",
    "role": "CUSTOMER",
    "userId": 123,
    "message": "Login successful"
  },
  "error": null
}
```

### 4. Request OTP

**POST** `/auth/request-otp`

Request OTP for various operations.

**Request Body:**
```json
{
  "email": "customer@example.com",
  "otpType": "TRANSACTION"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP sent to your email",
  "data": null,
  "error": null
}
```

## Customer Portal Endpoints

### Dashboard

**GET** `/customer/dashboard`

Get customer dashboard with overview.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Dashboard loaded",
  "data": {
    "customer": {
      "customerId": 123,
      "email": "customer@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "kycStatus": "APPROVED",
      "accountType": "ADULT"
    },
    "totalAccounts": 2,
    "totalBalance": 150000.00,
    "totalCards": 3,
    "totalLoans": 1,
    "unreadNotifications": 5
  }
}
```

### Accounts

**GET** `/customer/accounts`

Get all customer accounts.

**Response:**
```json
{
  "success": true,
  "message": "Accounts loaded",
  "data": [
    {
      "accountId": 1,
      "accountNumber": "1234567890123456",
      "accountType": "SAVINGS",
      "balance": 75000.00,
      "currency": "INR",
      "status": "ACTIVE",
      "interestRate": 4.00,
      "openedDate": "2024-01-15"
    }
  ]
}
```

### Account Transactions

**GET** `/customer/accounts/{accountId}/transactions`

Get transactions for a specific account.

**Query Parameters:**
- `startDate` (optional) - Format: YYYY-MM-DD
- `endDate` (optional) - Format: YYYY-MM-DD

**Response:**
```json
{
  "success": true,
  "message": "Transactions loaded",
  "data": [
    {
      "transactionId": 1001,
      "accountNumber": "1234567890123456",
      "transactionType": "TRANSFER_OUT",
      "amount": 5000.00,
      "balanceAfter": 70000.00,
      "counterpartyAccount": "9876543210987654",
      "description": "Payment to vendor",
      "referenceNumber": "TXN1234567890ABCD",
      "status": "SUCCESS",
      "transactionDate": "2024-02-06T10:30:00"
    }
  ]
}
```

### Make Transaction

**POST** `/customer/transactions`

Execute a transaction (requires OTP verification).

**Request Body:**
```json
{
  "accountId": 1,
  "transactionType": "TRANSFER_OUT",
  "amount": 5000.00,
  "counterpartyAccount": "9876543210987654",
  "description": "Payment to vendor",
  "otpCode": "123456"
}
```

**Transaction Types:**
- `DEPOSIT` - Deposit money
- `WITHDRAWAL` - Withdraw money
- `TRANSFER_OUT` - Transfer to another account

**Response:**
```json
{
  "success": true,
  "message": "Transaction successful",
  "data": {
    "transactionId": 1001,
    "referenceNumber": "TXN1234567890ABCD",
    "status": "SUCCESS",
    "balanceAfter": 70000.00
  }
}
```

### Cards

**GET** `/customer/cards`

Get all customer cards.

**Response:**
```json
{
  "success": true,
  "message": "Cards loaded",
  "data": [
    {
      "cardId": 1,
      "cardNumber": "**** **** **** 1234",
      "cardType": "DEBIT",
      "cardHolderName": "JOHN DOE",
      "expiryDate": "2027-12-31",
      "status": "ACTIVE",
      "creditLimit": 0.00,
      "availableCredit": 0.00,
      "isInternationalEnabled": false,
      "isOnlineEnabled": true
    }
  ]
}
```

### Card Action

**POST** `/customer/cards/action`

Perform card action (requires OTP).

**Request Body:**
```json
{
  "cardId": 1,
  "action": "BLOCK",
  "otpCode": "123456"
}
```

**Actions:**
- `BLOCK` - Block the card
- `UNBLOCK` - Unblock the card
- `REPLACE` - Request replacement

**Response:**
```json
{
  "success": true,
  "message": "Card action successful",
  "data": {
    "cardId": 1,
    "status": "BLOCKED"
  }
}
```

### KYC Upload

**POST** `/customer/kyc/upload`

Upload KYC document.

**Request Body:**
```json
{
  "customerId": 123,
  "documentType": "AADHAAR",
  "documentNumber": "1234-5678-9012",
  "documentUrl": "https://storage.example.com/docs/aadhaar.pdf"
}
```

**Document Types:**
- `AADHAAR` - Aadhaar card
- `PAN` - PAN card
- `PASSPORT` - Passport
- `DRIVING_LICENSE` - Driving license
- `VOTER_ID` - Voter ID
- `ADDRESS_PROOF` - Address proof
- `PHOTO` - Photograph

**Response:**
```json
{
  "success": true,
  "message": "KYC document uploaded",
  "data": {
    "kycId": 1,
    "documentType": "AADHAAR",
    "verificationStatus": "PENDING",
    "uploadDate": "2024-02-06T10:00:00"
  }
}
```

## Employee Portal Endpoints

### Employee Dashboard

**GET** `/employee/dashboard`

Get employee dashboard.

**Headers:**
```
Authorization: Bearer <employee_access_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Dashboard loaded",
  "data": {
    "employeeName": "Admin User",
    "department": "KYC_VERIFICATION",
    "designation": "KYC Verifier",
    "pendingKycDocuments": 25,
    "pendingLoanApplications": 10,
    "pendingKycApprovals": 15
  }
}
```

### Pending KYC Documents

**GET** `/employee/kyc/pending`

Get all pending KYC documents.

**Response:**
```json
{
  "success": true,
  "message": "Pending KYC documents loaded",
  "data": [
    {
      "kycId": 1,
      "customerId": 123,
      "documentType": "AADHAAR",
      "documentNumber": "1234-5678-9012",
      "documentUrl": "https://storage.example.com/docs/aadhaar.pdf",
      "verificationStatus": "PENDING",
      "uploadDate": "2024-02-06T10:00:00"
    }
  ]
}
```

### Customer KYC Documents

**GET** `/employee/kyc/customer/{customerId}`

Get all KYC documents for a specific customer.

**Response:**
```json
{
  "success": true,
  "message": "Customer KYC documents loaded",
  "data": {
    "customer": {
      "customerId": 123,
      "firstName": "John",
      "lastName": "Doe",
      "email": "customer@example.com",
      "kycStatus": "PENDING"
    },
    "documents": [
      {
        "kycId": 1,
        "documentType": "AADHAAR",
        "verificationStatus": "PENDING",
        "uploadDate": "2024-02-06T10:00:00"
      }
    ]
  }
}
```

### Verify KYC

**POST** `/employee/kyc/verify`

Approve or reject KYC document (requires employee OTP).

**Request Body:**
```json
{
  "kycId": 1,
  "status": "APPROVED",
  "rejectionReason": null,
  "employeeId": 456
}
```

**Status Values:**
- `APPROVED` - Approve the document
- `REJECTED` - Reject the document (requires rejectionReason)

**Response:**
```json
{
  "success": true,
  "message": "KYC verification completed",
  "data": {
    "kycId": 1,
    "verificationStatus": "APPROVED",
    "verificationDate": "2024-02-06T11:00:00"
  }
}
```

### Search Customers

**GET** `/employee/customers/search?query={searchTerm}`

Search for customers by name, email, or phone.

**Query Parameters:**
- `query` - Search term

**Response:**
```json
{
  "success": true,
  "message": "Search results",
  "data": [
    {
      "customerId": 123,
      "firstName": "John",
      "lastName": "Doe",
      "email": "customer@example.com",
      "phone": "+919876543210",
      "kycStatus": "APPROVED"
    }
  ]
}
```

## Error Responses

All endpoints may return error responses in this format:

```json
{
  "success": false,
  "message": "Operation failed",
  "data": null,
  "error": "Detailed error message here"
}
```

**Common HTTP Status Codes:**
- `200 OK` - Success
- `400 Bad Request` - Invalid request
- `401 Unauthorized` - Missing/invalid token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Rate Limiting

API endpoints are rate-limited to prevent abuse:
- Authentication: 5 requests per minute
- General endpoints: 100 requests per minute per user

## Security Notes

1. All endpoints use HTTPS
2. JWT tokens expire after 24 hours
3. Refresh tokens expire after 7 days
4. OTPs expire after 10 minutes
5. Maximum 3 OTP attempts per request
6. All sensitive operations require OTP verification
7. Passwords must be at least 8 characters with special characters
8. All actions are logged in audit_log table

## Testing with curl

### Register
```bash
curl -X POST https://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@123",
    "firstName": "Test",
    "lastName": "User",
    "dateOfBirth": "1995-01-01",
    "phone": "+919876543210",
    "accountType": "ADULT"
  }'
```

### Login
```bash
curl -X POST https://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@123"
  }'
```

### Get Dashboard (with token)
```bash
curl -X GET https://localhost:8080/api/customer/dashboard \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

For more information, refer to the main [README.md](README.md) documentation.
