# FinSecure Banking System

A production-ready full-stack banking application built with Java Spring Boot 3 and React 18.

## Architecture

```
finsecure/
├── backend/          # Spring Boot 3 + Java 17 + MySQL
├── customer-portal/  # React 18 (Port 3000)
└── employee-portal/  # React 18 (Port 3001)
```

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+

### Backend
```bash
cd backend
# Edit src/main/resources/application.properties with your MySQL credentials
mvn clean install
mvn spring-boot:run
```

### Customer Portal
```bash
cd customer-portal
npm install
npm start
# Runs at http://localhost:3000
```

### Employee Portal
```bash
cd employee-portal
npm install
npm start
# Runs at http://localhost:3001
```

## Demo Credentials

| Role     | Email                | Password       |
|----------|----------------------|----------------|
| Customer | priya@gmail.com      | Customer@1234  |
| Employee | emp1@finsecure.com   | Employee@1234  |
| Admin    | admin@finsecure.com  | Admin@1234     |

## Features

### Customer Portal
- Registration with email verification (OTP)
- Secure JWT authentication
- Dashboard with account overview
- Multi-account management (Savings, Current, FD, RD)
- Fund transfers (NEFT, RTGS, IMPS, UPI)
- OTP verification for transactions > ₹10,000
- Loan applications with EMI calculation
- KYC document management
- Debit/Credit card management
- Transaction history with pagination

### Employee Portal
- Customer management & search
- KYC document verification (Approve/Reject)
- Loan application processing
- Audit trails

### Security
- JWT authentication (JJWT 0.12.5 modern API)
- BCrypt password hashing (strength 12)
- Role-based access control (RBAC)
- CORS configuration for localhost:3000 and localhost:3001
- OTP-based transaction verification
- Audit logging for all actions
- Database triggers for business rules

## API Endpoints

### Public
- POST `/api/auth/register`
- POST `/api/auth/login`
- POST `/api/auth/otp/send`
- POST `/api/auth/otp/verify`

### Customer (JWT Required)
- GET `/api/customer/dashboard`
- GET `/api/customer/profile`
- POST `/api/customer/accounts`
- POST `/api/customer/transactions/transfer`
- GET `/api/customer/transactions/{accountId}`
- POST `/api/customer/loans/apply`
- GET `/api/customer/loans`
- GET `/api/customer/cards`
- POST `/api/customer/cards/{accountId}/issue-debit`
- POST `/api/customer/kyc/upload`

### Employee (JWT Required + EMPLOYEE/ADMIN role)
- GET `/api/employee/customers`
- GET `/api/employee/kyc/pending`
- POST `/api/employee/kyc/verify`
- GET `/api/employee/loans/pending`
- POST `/api/employee/loans/{id}/review`
