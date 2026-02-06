package com.finsecure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * FinSecure Banking System - Main Application
 * 
 * A comprehensive banking solution with:
 * - Customer Portal (Account management, transactions, cards, loans)
 * - Employee Portal (KYC verification, customer service, loan processing)
 * - JWT-based authentication with RBAC
 * - Email-based OTP verification
 * - End-to-end encryption (HTTPS/TLS)
 * - Complete audit logging
 * 
 * @author FinSecure Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class FinSecureApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinSecureApplication.class, args);
        System.out.println("========================================");
        System.out.println("FinSecure Banking System Started");
        System.out.println("========================================");
        System.out.println("Customer Portal: https://localhost:3000");
        System.out.println("Employee Portal: https://localhost:3001");
        System.out.println("API Endpoint: https://localhost:8080/api");
        System.out.println("========================================");
    }
}
