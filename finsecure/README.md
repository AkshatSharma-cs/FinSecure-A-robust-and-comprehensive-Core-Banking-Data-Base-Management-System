# FinSecure Banking System

A comprehensive, production-ready full-stack banking application with separate Customer and Employee portals, implementing industry-standard security practices and complete banking features.

## 🏗️ Architecture Overview

### System Components

```
┌─────────────────────────────────────────────────────────┐
│                    FinSecure System                     │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────────┐         ┌──────────────────┐    │
│  │  Customer Portal │         │ Employee Portal  │    │
│  │   (Port 3000)    │         │   (Port 3001)    │    │
│  │     React UI     │         │    React UI      │    │
│  └────────┬─────────┘         └─────────┬────────┘    │
│           │                             │              │
│           └──────────┬──────────────────┘              │
│                      │                                 │
│           ┌──────────▼──────────┐                      │
│           │   Spring Boot API   │                      │
│           │    (Port 8080)      │                      │
│           │   JWT + HTTPS/TLS   │                      │
│           └──────────┬──────────┘                      │
│                      │                                 │
│           ┌──────────▼──────────┐                      │
│           │  PostgreSQL / MySQL │                      │
│           │     Database        │                      │
│           └─────────────────────┘                      │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Technology Stack

**Backend:**
- Spring Boot 3.2.0 (Java 17)
- Spring Security with JWT authentication
- Spring Data JPA with Hibernate
- PostgreSQL / MySQL database
- BCrypt password hashing
- Email service (SMTP)

**Frontend:**
- React 18
- React Router v6
- Axios for API calls
- CSS3 for styling

**Security:**
- HTTPS/TLS encryption
- JWT token-based authentication
- Role-based access control (RBAC)
- Email-based OTP verification
- BCrypt/Argon2 password hashing
- Comprehensive audit logging

## 📋 Features

### Customer Portal Features

1. **Account Management**
   - Multiple account types (Savings, Current, Fixed Deposit)
   - Real-time balance tracking
   - Transaction history with filtering
   - Account statements

2. **Transactions**
   - Fund transfers (OTP-verified)
   - Deposits and withdrawals
   - Transaction limits based on account type
   - Real-time balance updates

3. **Card Management**
   - Debit/Credit/Prepaid cards
   - Card blocking (OTP-verified)
   - Card replacement requests
   - International/Online transaction toggles
   - Student and commercial cards

4. **Loan Services**
   - Loan applications (Personal, Home, Auto, Education, Business)
   - EMI tracking
   - Loan status monitoring
   - Early repayment options

5. **KYC Management**
   - Document upload (Aadhaar, PAN, Passport, etc.)
   - KYC status tracking
   - Re-upload for rejected documents
   - Automatic account activation on approval

6. **Account Types**
   - **Student Account**: College-age users, limited features
   - **Adult Account**: Full banking features
   - **Minor Account**: Guardian-linked, restricted access, requires conversion to major

### Employee Portal Features

1. **KYC Verification**
   - Review pending KYC documents
   - View customer information
   - Approve/Reject with OTP verification
   - Bulk document processing
   - Audit trail

2. **Customer Management**
   - Search customers
   - View customer details
   - Account overview
   - Transaction monitoring

3. **Loan Processing**
   - Review loan applications
   - Approve/Reject loans
   - Disbursement tracking

4. **Department-based Access**
   - KYC Verification Team
   - Customer Service
   - Loan Processing
   - Compliance
   - IT
   - Management

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js 16+ and npm
- PostgreSQL 13+ or MySQL 8+
- SMTP server (for email)

### Database Setup

1. **MySQL Setup (Default)**

```bash
# Login to MySQL
mysql -u root -p

# Run the complete setup script (creates database, user, and all tables)
mysql -u root -p < backend/database-schema-mysql.sql
```

Or manually:

```bash
# Create database with proper charset
mysql -u root -p

# In MySQL:
CREATE DATABASE finsecure_db 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE USER 'finsecure_user'@'localhost' IDENTIFIED BY 'finsecure_pass';
GRANT ALL PRIVILEGES ON finsecure_db.* TO 'finsecure_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# Import schema
mysql -u finsecure_user -p finsecure_db < backend/database-schema-mysql.sql
```

2. **PostgreSQL Setup (Alternative)**

```bash
# Create database
createdb finsecure_db

# Create user
psql -c "CREATE USER finsecure_user WITH PASSWORD 'finsecure_pass';"
psql -c "GRANT ALL PRIVILEGES ON DATABASE finsecure_db TO finsecure_user;"

# Run schema
psql -U finsecure_user -d finsecure_db -f backend/database-schema.sql

# Update application.properties:
# - Change spring.datasource.url to PostgreSQL
# - Change spring.jpa.properties.hibernate.dialect to PostgreSQLDialect
```

### Backend Setup

1. **Configure Application Properties**

```bash
cd backend/src/main/resources
nano application.properties
```

Update the following:
- Database connection (PostgreSQL/MySQL)
- JWT secret key (change to secure random value)
- Email SMTP settings
- File upload directory

2. **Generate SSL Certificate (for HTTPS)**

```bash
cd backend/src/main/resources
keytool -genkeypair -alias finsecure -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -validity 3650
```

3. **Build and Run**

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will start on: `https://localhost:8080/api`

### Frontend Setup

#### Customer Portal

```bash
cd frontend/customer-portal
npm install
npm start
```

Customer Portal: `https://localhost:3000`

#### Employee Portal

```bash
cd frontend/employee-portal
npm install
npm start
```

Employee Portal: `https://localhost:3001`

## 📊 Database Schema

### Core Tables

- **User**: Authentication and role management
- **Customer**: Customer profile information
- **Employee**: Employee details and departments
- **Account**: Bank accounts (Savings, Current, FD, RD)
- **Transaction**: All financial transactions
- **Card**: Debit/Credit/Prepaid cards
- **Loan**: Loan applications and tracking
- **Loan_EMI**: EMI schedule and payments
- **Investment**: Investment products
- **Insurance_Policy**: Insurance policies
- **KYC_Document**: KYC verification documents
- **OTP**: OTP verification records
- **Notification**: User notifications
- **Audit_Log**: Comprehensive audit trail
- **Ads**: Banking advertisements

### Relationships

- One-to-One: User ↔ Customer, User ↔ Employee
- One-to-Many: Customer → Accounts, Customer → Cards, Customer → Loans
- Many-to-One: Account → Customer, Transaction → Account
- Self-referencing: Customer (guardian) → Customer (minor)

## 🔐 Security Features

### Authentication Flow

1. User submits email + password
2. Backend validates credentials
3. OTP sent to email
4. User verifies OTP
5. JWT tokens issued (access + refresh)
6. Tokens used for subsequent requests

### OTP Verification Points

- Login (optional)
- Fund transfers
- Card blocking/replacement
- Password changes
- Profile updates
- KYC confirmation (employees)

### Role-Based Access Control

- **CUSTOMER**: Access to customer portal only
- **EMPLOYEE**: Access to employee portal, department-based features
- **ADMIN**: Full system access

## 📱 API Endpoints

### Authentication

```
POST /api/auth/register       - Register new customer
POST /api/auth/login          - Login with credentials
POST /api/auth/verify-otp     - Verify OTP
POST /api/auth/request-otp    - Request new OTP
POST /api/auth/logout         - Logout
```

### Customer Portal

```
GET  /api/customer/dashboard                    - Get dashboard
GET  /api/customer/profile                      - Get profile
GET  /api/customer/accounts                     - Get all accounts
GET  /api/customer/accounts/{id}/transactions   - Get transactions
POST /api/customer/transactions                 - Make transaction
GET  /api/customer/cards                        - Get all cards
POST /api/customer/cards/action                 - Card action (block/unblock)
GET  /api/customer/loans                        - Get all loans
POST /api/customer/kyc/upload                   - Upload KYC
GET  /api/customer/kyc/documents                - Get KYC docs
GET  /api/customer/notifications                - Get notifications
```

### Employee Portal

```
GET  /api/employee/dashboard                - Employee dashboard
GET  /api/employee/kyc/pending              - Pending KYC docs
GET  /api/employee/kyc/customer/{id}        - Customer KYC
POST /api/employee/kyc/verify               - Verify KYC
GET  /api/employee/customers/pending-kyc    - Pending KYC customers
GET  /api/employee/customers/{id}           - Customer details
GET  /api/employee/customers/search         - Search customers
GET  /api/employee/loans/pending            - Pending loans
```

## 🧪 Testing

### Default Admin Credentials

```
Email: admin@finsecure.com
Password: Admin@123
```

**⚠️ IMPORTANT: Change this password immediately in production!**

### Test Customer Registration

1. Navigate to Customer Portal
2. Click "Register"
3. Fill in details:
   - Choose account type (STUDENT/ADULT)
   - For MINOR accounts, guardian linking is required
4. Submit and verify email OTP
5. Upload KYC documents
6. Wait for employee verification

### Test Employee Functions

1. Login to Employee Portal
2. Navigate to KYC Verification
3. Review pending documents
4. Approve/Reject with OTP

## 📂 Project Structure

```
finsecure/
├── backend/
│   ├── src/main/
│   │   ├── java/com/finsecure/
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── repository/       # Data repositories
│   │   │   ├── service/          # Business logic
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── security/         # Security config
│   │   │   ├── dto/              # Data transfer objects
│   │   │   ├── config/           # Spring configuration
│   │   │   └── util/             # Utility classes
│   │   └── resources/
│   │       └── application.properties
│   ├── pom.xml
│   └── database-schema.sql
├── frontend/
│   ├── customer-portal/
│   │   ├── src/
│   │   │   ├── components/       # React components
│   │   │   ├── utils/            # API utilities
│   │   │   └── App.js
│   │   └── package.json
│   └── employee-portal/
│       ├── src/
│       │   ├── components/
│       │   ├── utils/
│       │   └── App.js
│       └── package.json
└── README.md
```

## 🔧 Configuration

### Account Type Restrictions

**Student Account:**
- Daily transaction limit: ₹50,000
- Limited card features
- No credit cards

**Adult Account:**
- Daily transaction limit: ₹500,000
- Full features
- Credit cards available (post-KYC)

**Minor Account:**
- Daily transaction limit: ₹10,000
- Guardian approval required
- No credit cards
- Must be converted to adult at age 18

### KYC Requirements

Minimum 3 documents required:
- Identity proof (Aadhaar/PAN/Passport/DL/Voter ID)
- Address proof
- Photo

## 🚨 Important Security Notes

1. **Change Default Credentials**: Update admin password immediately
2. **JWT Secret**: Use a strong, random 256-bit key
3. **HTTPS**: Always use HTTPS in production
4. **Database**: Use strong passwords and restrict access
5. **Email**: Use app-specific passwords for Gmail
6. **File Upload**: Validate and scan uploaded files
7. **Rate Limiting**: Implement API rate limiting
8. **Audit Logs**: Regularly review audit logs

## 📝 Production Deployment Checklist

- [ ] Change all default passwords
- [ ] Generate production SSL certificates
- [ ] Configure production database
- [ ] Set up email service
- [ ] Enable CSRF protection
- [ ] Implement rate limiting
- [ ] Set up monitoring and logging
- [ ] Configure backup strategy
- [ ] Review and harden security settings
- [ ] Set up CDN for frontend
- [ ] Configure reverse proxy (nginx)
- [ ] Enable database encryption at rest
- [ ] Set up intrusion detection
- [ ] Configure firewalls

## 🐛 Troubleshooting

### Backend won't start
- Check Java version (must be 17+)
- Verify database connection
- Check if port 8080 is available
- Review application.properties

### Frontend can't connect to backend
- Verify backend is running on port 8080
- Check CORS configuration
- Ensure HTTPS certificates are trusted
- Check browser console for errors

### OTP not received
- Verify SMTP configuration
- Check email spam folder
- Review email service logs
- Ensure email address is valid

## 📄 License

This project is created for educational and demonstration purposes.

## 👥 Support

For issues and questions:
- Review this README
- Check the code comments
- Review Spring Boot and React documentation

## 🎯 Future Enhancements

- [ ] Mobile app (React Native)
- [ ] Biometric authentication
- [ ] Real-time notifications (WebSocket)
- [ ] Investment portfolio tracking
- [ ] Bill payment integration
- [ ] Chatbot support
- [ ] Multi-language support
- [ ] Advanced analytics dashboard
- [ ] Recurring payments
- [ ] Virtual cards

---

**Built with ❤️ using Spring Boot and React**
