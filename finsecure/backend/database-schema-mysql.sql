-- FinSecure Banking System - MySQL Database Schema
-- MySQL 8.0+
-- Character Set: utf8mb4 (full Unicode support including emojis)
-- Collation: utf8mb4_unicode_ci (case-insensitive Unicode)

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- Create database with proper character set
CREATE DATABASE IF NOT EXISTS finsecure_db 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE finsecure_db;

-- =====================================================
-- CORE USER TABLES
-- =====================================================

DROP TABLE IF EXISTS User;
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

DROP TABLE IF EXISTS Customer;
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
    guardian_customer_id BIGINT NULL,
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

DROP TABLE IF EXISTS Employee;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ACCOUNT & TRANSACTION TABLES
-- =====================================================

DROP TABLE IF EXISTS Account;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS Transaction;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- CARD MANAGEMENT TABLES
-- =====================================================

DROP TABLE IF EXISTS Card;
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
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- LOAN MANAGEMENT TABLES
-- =====================================================

DROP TABLE IF EXISTS Loan;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS Loan_EMI;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INVESTMENT & INSURANCE TABLES
-- =====================================================

DROP TABLE IF EXISTS Investment;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS Insurance_Policy;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- KYC & VERIFICATION TABLES
-- =====================================================

DROP TABLE IF EXISTS KYC_Document;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- SECURITY & OTP TABLES
-- =====================================================

DROP TABLE IF EXISTS OTP;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS Audit_Log;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- NOTIFICATION & COMMUNICATION TABLES
-- =====================================================

DROP TABLE IF EXISTS Notification;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS Ads;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

CREATE INDEX idx_customer_kyc_lookup ON Customer(customer_id, kyc_status);
CREATE INDEX idx_account_customer_lookup ON Account(customer_id, status);
CREATE INDEX idx_transaction_account_date ON Transaction(account_id, transaction_date);
CREATE INDEX idx_card_customer_status ON Card(customer_id, status);
CREATE INDEX idx_loan_customer_status ON Loan(customer_id, status);

-- =====================================================
-- VIEWS FOR COMMON QUERIES
-- =====================================================

CREATE OR REPLACE VIEW vw_customer_dashboard AS
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
DROP TRIGGER IF EXISTS trg_kyc_approval_account_activation//
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
DROP TRIGGER IF EXISTS trg_kyc_rejection_card_block//
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

-- Insert default admin employee
INSERT INTO Employee (user_id, first_name, last_name, employee_code, department, designation, phone, hire_date) 
VALUES (1, 'System', 'Admin', 'EMP001', 'MANAGEMENT', 'System Administrator', '+919999999999', CURDATE());

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Show all tables
SHOW TABLES;

-- Show table counts
SELECT 
    'User' as table_name, COUNT(*) as count FROM User
UNION ALL
SELECT 'Customer', COUNT(*) FROM Customer
UNION ALL
SELECT 'Employee', COUNT(*) FROM Employee
UNION ALL
SELECT 'Account', COUNT(*) FROM Account
UNION ALL
SELECT 'Transaction', COUNT(*) FROM Transaction
UNION ALL
SELECT 'Card', COUNT(*) FROM Card
UNION ALL
SELECT 'Loan', COUNT(*) FROM Loan
UNION ALL
SELECT 'KYC_Document', COUNT(*) FROM KYC_Document
UNION ALL
SELECT 'OTP', COUNT(*) FROM OTP
UNION ALL
SELECT 'Notification', COUNT(*) FROM Notification
UNION ALL
SELECT 'Audit_Log', COUNT(*) FROM Audit_Log;

-- Note: Default password is 'Admin@123'
-- In production, change this immediately after first login
