-- FinSecure Banking System - Database Schema
-- Database: MySQL 8.0+

-- =====================================================
-- CORE USER TABLES
-- =====================================================

CREATE TABLE User (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER', 'EMPLOYEE', 'ADMIN') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Customer (
    customer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100) DEFAULT 'India',
    postal_code VARCHAR(20),
    account_type ENUM('STUDENT', 'ADULT', 'MINOR') NOT NULL,
    kyc_status ENUM('PENDING', 'APPROVED', 'REJECTED', 'INCOMPLETE') DEFAULT 'PENDING',
    guardian_customer_id BIGINT NULL, -- For MINOR accounts
    profile_image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE,
    FOREIGN KEY (guardian_customer_id) REFERENCES Customer(customer_id) ON DELETE SET NULL,
    INDEX idx_kyc_status (kyc_status),
    INDEX idx_account_type (account_type),
    CONSTRAINT chk_adult_age CHECK (
        (account_type != 'ADULT') OR 
        (TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()) >= 18)
    ),
    CONSTRAINT chk_minor_age CHECK (
        (account_type != 'MINOR') OR 
        (TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()) < 18)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Employee (
    employee_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    employee_code VARCHAR(50) UNIQUE NOT NULL,
    department ENUM('KYC_VERIFICATION', 'CUSTOMER_SERVICE', 'LOAN_PROCESSING', 'COMPLIANCE', 'IT', 'MANAGEMENT') NOT NULL,
    designation VARCHAR(100),
    phone VARCHAR(20) UNIQUE NOT NULL,
    hire_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE,
    INDEX idx_department (department)
);

-- =====================================================
-- ACCOUNT & TRANSACTION TABLES
-- =====================================================

CREATE TABLE Account (
    account_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_type ENUM('SAVINGS', 'CURRENT', 'FIXED_DEPOSIT', 'RECURRING_DEPOSIT') NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'INR',
    status ENUM('PENDING', 'ACTIVE', 'FROZEN', 'CLOSED') DEFAULT 'PENDING',
    interest_rate DECIMAL(5, 2) DEFAULT 0.00,
    minimum_balance DECIMAL(10, 2) DEFAULT 0.00,
    opened_date DATE NOT NULL,
    closed_date DATE NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
    INDEX idx_account_number (account_number),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    CONSTRAINT chk_balance CHECK (balance >= 0)
);

CREATE TABLE Transaction (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT', 'INTEREST_CREDIT', 'FEE_DEBIT', 'LOAN_DISBURSEMENT', 'LOAN_REPAYMENT') NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    balance_after DECIMAL(15, 2) NOT NULL,
    counterparty_account VARCHAR(20) NULL,
    description TEXT,
    reference_number VARCHAR(50) UNIQUE NOT NULL,
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REVERSED') DEFAULT 'PENDING',
    otp_verified BOOLEAN DEFAULT FALSE,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES Account(account_id) ON DELETE CASCADE,
    INDEX idx_account_id (account_id),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_reference_number (reference_number),
    INDEX idx_status (status),
    CONSTRAINT chk_amount CHECK (amount > 0)
);

-- =====================================================
-- CARD MANAGEMENT TABLES
-- =====================================================

CREATE TABLE Card (
    card_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    card_number VARCHAR(19) UNIQUE NOT NULL,
    card_type ENUM('DEBIT', 'CREDIT', 'PREPAID', 'STUDENT', 'COMMERCIAL') NOT NULL,
    card_holder_name VARCHAR(100) NOT NULL,
    cvv_hash VARCHAR(255) NOT NULL,
    expiry_date DATE NOT NULL,
    issue_date DATE NOT NULL,
    credit_limit DECIMAL(12, 2) DEFAULT 0.00,
    available_credit DECIMAL(12, 2) DEFAULT 0.00,
    status ENUM('ACTIVE', 'BLOCKED', 'EXPIRED', 'REPLACED', 'PENDING_ACTIVATION') DEFAULT 'PENDING_ACTIVATION',
    pin_hash VARCHAR(255),
    is_international_enabled BOOLEAN DEFAULT FALSE,
    is_online_enabled BOOLEAN DEFAULT TRUE,
    replacement_requested BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES Account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
    INDEX idx_card_number (card_number),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    CONSTRAINT chk_credit_card_minor CHECK (
        (card_type != 'CREDIT') OR 
        (customer_id IN (SELECT customer_id FROM Customer WHERE account_type != 'MINOR'))
    )
);

-- =====================================================
-- LOAN MANAGEMENT TABLES
-- =====================================================

CREATE TABLE Loan (
    loan_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    loan_type ENUM('PERSONAL', 'HOME', 'AUTO', 'EDUCATION', 'BUSINESS') NOT NULL,
    loan_amount DECIMAL(15, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    tenure_months INT NOT NULL,
    emi_amount DECIMAL(12, 2) NOT NULL,
    outstanding_amount DECIMAL(15, 2) NOT NULL,
    status ENUM('APPLIED', 'APPROVED', 'DISBURSED', 'ACTIVE', 'CLOSED', 'REJECTED', 'DEFAULTED') DEFAULT 'APPLIED',
    application_date DATE NOT NULL,
    approval_date DATE NULL,
    disbursement_date DATE NULL,
    closure_date DATE NULL,
    approved_by_employee_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES Account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by_employee_id) REFERENCES Employee(employee_id) ON DELETE SET NULL,
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    CONSTRAINT chk_loan_amount CHECK (loan_amount > 0),
    CONSTRAINT chk_tenure CHECK (tenure_months > 0)
);

CREATE TABLE Loan_EMI (
    emi_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_id BIGINT NOT NULL,
    emi_number INT NOT NULL,
    due_date DATE NOT NULL,
    emi_amount DECIMAL(12, 2) NOT NULL,
    principal_component DECIMAL(12, 2) NOT NULL,
    interest_component DECIMAL(12, 2) NOT NULL,
    payment_date DATE NULL,
    status ENUM('PENDING', 'PAID', 'OVERDUE', 'WAIVED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (loan_id) REFERENCES Loan(loan_id) ON DELETE CASCADE,
    INDEX idx_loan_id (loan_id),
    INDEX idx_due_date (due_date),
    INDEX idx_status (status),
    UNIQUE KEY unique_loan_emi (loan_id, emi_number)
);

-- =====================================================
-- INVESTMENT & INSURANCE TABLES
-- =====================================================

CREATE TABLE Investment (
    investment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    investment_type ENUM('MUTUAL_FUND', 'FIXED_DEPOSIT', 'RECURRING_DEPOSIT', 'BONDS', 'STOCKS') NOT NULL,
    investment_name VARCHAR(200) NOT NULL,
    invested_amount DECIMAL(15, 2) NOT NULL,
    current_value DECIMAL(15, 2) NOT NULL,
    maturity_date DATE NULL,
    interest_rate DECIMAL(5, 2) DEFAULT 0.00,
    status ENUM('ACTIVE', 'MATURED', 'WITHDRAWN', 'CLOSED') DEFAULT 'ACTIVE',
    start_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES Account(account_id) ON DELETE CASCADE,
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status)
);

CREATE TABLE Insurance_Policy (
    policy_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    policy_number VARCHAR(50) UNIQUE NOT NULL,
    policy_type ENUM('LIFE', 'HEALTH', 'VEHICLE', 'HOME', 'TRAVEL') NOT NULL,
    policy_name VARCHAR(200) NOT NULL,
    sum_assured DECIMAL(15, 2) NOT NULL,
    premium_amount DECIMAL(10, 2) NOT NULL,
    premium_frequency ENUM('MONTHLY', 'QUARTERLY', 'HALF_YEARLY', 'YEARLY') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('ACTIVE', 'LAPSED', 'MATURED', 'CLAIMED', 'CANCELLED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
    INDEX idx_customer_id (customer_id),
    INDEX idx_policy_number (policy_number),
    INDEX idx_status (status)
);

-- =====================================================
-- KYC & VERIFICATION TABLES
-- =====================================================

CREATE TABLE KYC_Document (
    kyc_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    document_type ENUM('AADHAAR', 'PAN', 'PASSPORT', 'DRIVING_LICENSE', 'VOTER_ID', 'ADDRESS_PROOF', 'PHOTO') NOT NULL,
    document_number VARCHAR(100),
    document_url VARCHAR(500) NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verification_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    verified_by_employee_id BIGINT NULL,
    verification_date TIMESTAMP NULL,
    rejection_reason TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by_employee_id) REFERENCES Employee(employee_id) ON DELETE SET NULL,
    INDEX idx_customer_id (customer_id),
    INDEX idx_verification_status (verification_status)
);

-- =====================================================
-- SECURITY & OTP TABLES
-- =====================================================

CREATE TABLE OTP (
    otp_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    otp_type ENUM('LOGIN', 'TRANSACTION', 'CARD_BLOCK', 'PASSWORD_RESET', 'PROFILE_UPDATE', 'KYC_CONFIRMATION') NOT NULL,
    email VARCHAR(255) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_otp_code (otp_code),
    INDEX idx_expires_at (expires_at)
);

CREATE TABLE Audit_Log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    action_type ENUM('LOGIN', 'LOGOUT', 'FAILED_LOGIN', 'KYC_UPLOAD', 'KYC_APPROVE', 'KYC_REJECT', 'TRANSACTION', 'CARD_BLOCK', 'CARD_UNBLOCK', 'CARD_REPLACE', 'PASSWORD_CHANGE', 'PROFILE_UPDATE', 'ACCOUNT_CREATE', 'LOAN_APPLY', 'INVESTMENT_CREATE') NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_timestamp (timestamp)
);

-- =====================================================
-- NOTIFICATION & COMMUNICATION TABLES
-- =====================================================

CREATE TABLE Notification (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type ENUM('INFO', 'SUCCESS', 'WARNING', 'ALERT', 'KYC_STATUS', 'TRANSACTION', 'LOAN_STATUS', 'PAYMENT_DUE') NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
);

CREATE TABLE Ads (
    ad_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    target_audience ENUM('ALL', 'STUDENT', 'ADULT', 'BUSINESS') DEFAULT 'ALL',
    link_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_is_active (is_active),
    INDEX idx_target_audience (target_audience)
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Composite indexes for common queries
CREATE INDEX idx_customer_kyc_lookup ON Customer(customer_id, kyc_status);
CREATE INDEX idx_account_customer_lookup ON Account(customer_id, status);
CREATE INDEX idx_transaction_account_date ON Transaction(account_id, transaction_date);
CREATE INDEX idx_card_customer_status ON Card(customer_id, status);
CREATE INDEX idx_loan_customer_status ON Loan(customer_id, status);

-- =====================================================
-- VIEWS FOR COMMON QUERIES
-- =====================================================

CREATE VIEW vw_customer_dashboard AS
SELECT 
    c.customer_id,
    c.first_name,
    c.last_name,
    c.kyc_status,
    c.account_type,
    COUNT(DISTINCT a.account_id) as total_accounts,
    COALESCE(SUM(a.balance), 0) as total_balance,
    COUNT(DISTINCT card.card_id) as total_cards,
    COUNT(DISTINCT l.loan_id) as total_loans
FROM Customer c
LEFT JOIN Account a ON c.customer_id = a.customer_id AND a.status = 'ACTIVE'
LEFT JOIN Card card ON c.customer_id = card.customer_id AND card.status = 'ACTIVE'
LEFT JOIN Loan l ON c.customer_id = l.customer_id AND l.status IN ('ACTIVE', 'DISBURSED')
GROUP BY c.customer_id, c.first_name, c.last_name, c.kyc_status, c.account_type;

-- =====================================================
-- TRIGGERS FOR AUTOMATION
-- =====================================================

DELIMITER //

-- Trigger to update Account status when KYC is approved
CREATE TRIGGER trg_kyc_approval_account_activation
AFTER UPDATE ON Customer
FOR EACH ROW
BEGIN
    IF NEW.kyc_status = 'APPROVED' AND OLD.kyc_status != 'APPROVED' THEN
        UPDATE Account 
        SET status = 'ACTIVE' 
        WHERE customer_id = NEW.customer_id 
        AND status = 'PENDING';
    END IF;
END//

-- Trigger to update card status when KYC is rejected
CREATE TRIGGER trg_kyc_rejection_card_block
AFTER UPDATE ON Customer
FOR EACH ROW
BEGIN
    IF NEW.kyc_status = 'REJECTED' AND OLD.kyc_status != 'REJECTED' THEN
        UPDATE Card 
        SET status = 'BLOCKED' 
        WHERE customer_id = NEW.customer_id 
        AND status NOT IN ('EXPIRED', 'REPLACED');
    END IF;
END//

DELIMITER ;

-- =====================================================
-- INITIAL DATA SEEDING
-- =====================================================

-- Insert default admin user (password: Admin@123 - bcrypt hash)
INSERT INTO User (email, password_hash, role, is_active) VALUES 
('admin@finsecure.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8TvW.Q7vzGPzE7xXg3vGO6S2f1j8i', 'ADMIN', TRUE);

-- Note: Default password is 'Admin@123'
-- In production, change this immediately after first login
