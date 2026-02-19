-- ============================================================
-- FinSecure Banking System - Complete MySQL Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS finsecure_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE finsecure_db;

-- ============================================================
-- TABLE: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN') NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_users_email (email),
    INDEX idx_users_username (username)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: customers
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    date_of_birth DATE NOT NULL,
    pan_number VARCHAR(10) UNIQUE,
    aadhar_number VARCHAR(12) UNIQUE,
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    pin_code VARCHAR(10),
    kyc_status ENUM('PENDING', 'SUBMITTED', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_customers_pan (pan_number),
    INDEX idx_customers_phone (phone)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: employees
-- ============================================================
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    joining_date DATE NOT NULL,
    department ENUM('CUSTOMER_SERVICE', 'LOANS', 'KYC', 'ACCOUNTS', 'MANAGEMENT', 'IT') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'ON_LEAVE') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_employees_emp_id (employee_id)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: accounts
-- ============================================================
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    account_type ENUM('SAVINGS', 'CURRENT', 'FIXED_DEPOSIT', 'RECURRING_DEPOSIT') NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    minimum_balance DECIMAL(15,2) NOT NULL DEFAULT 500.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status ENUM('ACTIVE', 'INACTIVE', 'FROZEN', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    ifsc_code VARCHAR(11) NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_accounts_number (account_number),
    INDEX idx_accounts_customer (customer_id)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: transactions
-- ============================================================
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reference_number VARCHAR(30) NOT NULL UNIQUE,
    account_id BIGINT NOT NULL,
    type ENUM('CREDIT', 'DEBIT') NOT NULL,
    mode ENUM('NEFT', 'RTGS', 'IMPS', 'UPI', 'ATM', 'CASH', 'ONLINE', 'CHEQUE') NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    description VARCHAR(500),
    target_account_number VARCHAR(20),
    status ENUM('SUCCESS', 'FAILED', 'PENDING', 'REVERSED') NOT NULL DEFAULT 'SUCCESS',
    failure_reason VARCHAR(200),
    created_at DATETIME(6) NOT NULL,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_transactions_ref (reference_number),
    INDEX idx_transactions_account (account_id),
    INDEX idx_transactions_created (created_at)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: loans
-- ============================================================
CREATE TABLE IF NOT EXISTS loans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_number VARCHAR(20) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    reviewed_by BIGINT,
    loan_type ENUM('HOME', 'PERSONAL', 'CAR', 'EDUCATION', 'BUSINESS', 'GOLD') NOT NULL,
    principal_amount DECIMAL(15,2) NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    tenure_months INT NOT NULL,
    emi_amount DECIMAL(12,2) NOT NULL,
    outstanding_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_interest DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status ENUM('APPLIED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'DISBURSED', 'ACTIVE', 'CLOSED', 'DEFAULTED') NOT NULL DEFAULT 'APPLIED',
    disbursement_date DATE,
    next_emi_date DATE,
    purpose VARCHAR(500),
    rejection_reason VARCHAR(500),
    closed_date DATE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES employees(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_loans_number (loan_number),
    INDEX idx_loans_customer (customer_id)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: kyc_documents
-- ============================================================
CREATE TABLE IF NOT EXISTS kyc_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    verified_by BIGINT,
    document_type ENUM('AADHAAR', 'PAN', 'PASSPORT', 'DRIVING_LICENSE', 'VOTER_ID', 'UTILITY_BILL', 'BANK_STATEMENT', 'SALARY_SLIP') NOT NULL,
    document_number VARCHAR(100) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(100),
    mime_type VARCHAR(50),
    status ENUM('UPLOADED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'UPLOADED',
    rejection_reason VARCHAR(500),
    verified_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES employees(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_kyc_customer (customer_id),
    INDEX idx_kyc_status (status)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: cards
-- ============================================================
CREATE TABLE IF NOT EXISTS cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    card_type ENUM('DEBIT', 'CREDIT', 'PREPAID') NOT NULL,
    masked_card_number VARCHAR(20) NOT NULL,
    card_number_hash VARCHAR(255) NOT NULL,
    card_holder_name VARCHAR(200) NOT NULL,
    expiry_date DATE NOT NULL,
    cvv_hash VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'BLOCKED', 'EXPIRED', 'CANCELLED', 'PENDING_ACTIVATION') NOT NULL DEFAULT 'ACTIVE',
    credit_limit DECIMAL(12,2),
    available_limit DECIMAL(12,2) DEFAULT 0.00,
    international_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    online_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    contactless_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_cards_number (masked_card_number),
    INDEX idx_cards_account (account_id)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: otps
-- ============================================================
CREATE TABLE IF NOT EXISTS otps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    purpose ENUM('EMAIL_VERIFICATION', 'LOGIN', 'TRANSACTION', 'PASSWORD_RESET', 'CARD_ACTIVATION') NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    attempt_count INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_otp_email (email),
    INDEX idx_otp_expires (expires_at)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: notifications
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type ENUM('TRANSACTION', 'LOAN', 'KYC', 'ACCOUNT', 'CARD', 'SECURITY', 'GENERAL') NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    reference_id VARCHAR(50),
    reference_type VARCHAR(30),
    created_at DATETIME(6) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_notif_user (user_id),
    INDEX idx_notif_read (is_read)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: audit_logs
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100) NOT NULL,
    resource_id VARCHAR(50),
    details VARCHAR(2000),
    ip_address VARCHAR(45),
    user_agent VARCHAR(200),
    result ENUM('SUCCESS', 'FAILURE', 'UNAUTHORIZED') NOT NULL DEFAULT 'SUCCESS',
    error_message VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB;

-- ============================================================
-- TRIGGER: Enforce KYC approval before issuing credit cards
-- ============================================================
DELIMITER $$

CREATE TRIGGER before_credit_card_insert
BEFORE INSERT ON cards
FOR EACH ROW
BEGIN
    DECLARE kyc_status_val VARCHAR(20);
    DECLARE customer_id_val BIGINT;

    IF NEW.card_type = 'CREDIT' THEN
        SELECT c.kyc_status INTO kyc_status_val
        FROM customers c
        INNER JOIN accounts a ON a.customer_id = c.id
        WHERE a.id = NEW.account_id
        LIMIT 1;

        IF kyc_status_val != 'APPROVED' THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Credit card cannot be issued without approved KYC';
        END IF;
    END IF;
END$$

CREATE TRIGGER audit_large_transaction
AFTER INSERT ON transactions
FOR EACH ROW
BEGIN
    IF NEW.amount > 100000 THEN
        INSERT INTO audit_logs (user_id, username, action, resource, resource_id, details, result, created_at)
        SELECT 
            u.id, u.username, 'LARGE_TRANSACTION', 'TRANSACTION', NEW.reference_number,
            CONCAT('Amount: ', NEW.amount, ' Mode: ', NEW.mode),
            'SUCCESS', NOW(6)
        FROM accounts a
        INNER JOIN customers c ON c.id = a.customer_id
        INNER JOIN users u ON u.id = c.user_id
        WHERE a.id = NEW.account_id
        LIMIT 1;
    END IF;
END$$

DELIMITER ;

-- ============================================================
-- SEED DATA
-- ============================================================

-- Admin user (password: Admin@1234)
INSERT INTO users (email, username, password, role, active, email_verified, created_at, updated_at)
VALUES ('admin@finsecure.com', 'admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TqL3g5QhWHGX/1eLLANt.PBHXmUO', 
        'ROLE_ADMIN', TRUE, TRUE, NOW(6), NOW(6));

-- Employee user (password: Employee@1234)
INSERT INTO users (email, username, password, role, active, email_verified, created_at, updated_at)
VALUES ('emp1@finsecure.com', 'emp.ram', '$2a$12$n9N5Wn8J5hFxG8K4tX4Y/uJ3Oj9jqbHqFLCMPLDa6z1cZV5w.F1tu',
        'ROLE_EMPLOYEE', TRUE, TRUE, NOW(6), NOW(6));

-- Employee profile
INSERT INTO employees (user_id, employee_id, first_name, last_name, phone, joining_date, department, status, created_at, updated_at)
SELECT id, 'EMP001', 'Ram', 'Kumar', '9876543210', '2023-01-15', 'KYC', 'ACTIVE', NOW(6), NOW(6)
FROM users WHERE email = 'emp1@finsecure.com';

-- Customer user (password: Customer@1234)
INSERT INTO users (email, username, password, role, active, email_verified, created_at, updated_at)
VALUES ('priya@gmail.com', 'priya.sharma', '$2a$12$n9N5Wn8J5hFxG8K4tX4Y/uJ3Oj9jqbHqFLCMPLDa6z1cZV5w.F1tu',
        'ROLE_CUSTOMER', TRUE, TRUE, NOW(6), NOW(6));

-- Customer profile
INSERT INTO customers (user_id, first_name, last_name, phone, date_of_birth, pan_number, aadhar_number, 
    address, city, state, pin_code, kyc_status, created_at, updated_at)
SELECT id, 'Priya', 'Sharma', '9988776655', '1992-05-15', 'ABCPS1234P', '123456789012',
    '42 MG Road', 'Bangalore', 'Karnataka', '560001', 'APPROVED', NOW(6), NOW(6)
FROM users WHERE email = 'priya@gmail.com';

-- Account for Priya
INSERT INTO accounts (account_number, customer_id, account_type, balance, minimum_balance, currency, 
    status, ifsc_code, branch_name, created_at, updated_at)
SELECT 'FINS1001234567', c.id, 'SAVINGS', 50000.00, 500.00, 'INR', 'ACTIVE', 'FINS0001234', 'Bangalore Main', NOW(6), NOW(6)
FROM customers c INNER JOIN users u ON u.id = c.user_id WHERE u.email = 'priya@gmail.com';

-- Sample transactions
INSERT INTO transactions (reference_number, account_id, type, mode, amount, balance_after, 
    description, status, created_at)
SELECT 'TXN202401001', a.id, 'CREDIT', 'NEFT', 10000.00, 50000.00, 'Salary credit', 'SUCCESS', NOW(6)
FROM accounts a WHERE a.account_number = 'FINS1001234567';

INSERT INTO transactions (reference_number, account_id, type, mode, amount, balance_after, 
    description, status, created_at)
SELECT 'TXN202401002', a.id, 'DEBIT', 'UPI', 2500.00, 47500.00, 'Electricity bill', 'SUCCESS', NOW(6) - INTERVAL 1 DAY
FROM accounts a WHERE a.account_number = 'FINS1001234567';

-- Notification
INSERT INTO notifications (user_id, type, title, message, is_read, created_at)
SELECT id, 'ACCOUNT', 'Welcome to FinSecure', 'Your account has been created successfully. Complete KYC to unlock all features.', FALSE, NOW(6)
FROM users WHERE email = 'priya@gmail.com';
