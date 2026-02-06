package com.finsecure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DTOs {

    // ============ Authentication DTOs ============

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private String email;
        private String role;
        private Long userId;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String phone;
        private String address;
        private String city;
        private String state;
        private String postalCode;
        private String accountType;
        private Long guardianCustomerId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtpRequest {
        private String email;
        private String otpType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtpVerificationRequest {
        private String email;
        private String otpCode;
        private String otpType;
    }

    // ============ Customer DTOs ============

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerProfileResponse {
        private Long customerId;
        private String email;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String phone;
        private String address;
        private String city;
        private String state;
        private String country;
        private String postalCode;
        private String accountType;
        private String kycStatus;
        private String profileImageUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardResponse {
        private CustomerProfileResponse customer;
        private Integer totalAccounts;
        private BigDecimal totalBalance;
        private Integer totalCards;
        private Integer totalLoans;
        private Integer unreadNotifications;
    }

    // ============ Account DTOs ============

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountResponse {
        private Long accountId;
        private String accountNumber;
        private String accountType;
        private BigDecimal balance;
        private String currency;
        private String status;
        private BigDecimal interestRate;
        private LocalDate openedDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateAccountRequest {
        private String accountType;
        private BigDecimal initialDeposit;
    }

    // ============ Transaction DTOs ============

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionRequest {
        private Long accountId;
        private String transactionType;
        private BigDecimal amount;
        private String counterpartyAccount;
        private String description;
        private String otpCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionResponse {
        private Long transactionId;
        private String accountNumber;
        private String transactionType;
        private BigDecimal amount;
        private BigDecimal balanceAfter;
        private String counterpartyAccount;
        private String description;
        private String referenceNumber;
        private String status;
        private LocalDateTime transactionDate;
    }

    // ============ Card DTOs ============

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardResponse {
        private Long cardId;
        private String cardNumber;
        private String cardType;
        private String cardHolderName;
        private LocalDate expiryDate;
        private String status;
        private BigDecimal creditLimit;
        private BigDecimal availableCredit;
        private Boolean isInternationalEnabled;
        private Boolean isOnlineEnabled;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardActionRequest {
        private Long cardId;
        private String action;
        private String otpCode;
    }

    // ============ Loan DTOs ============

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanApplicationRequest {
        private Long accountId;
        private String loanType;
        private BigDecimal loanAmount;
        private Integer tenureMonths;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanResponse {
        private Long loanId;
        private String loanType;
        private BigDecimal loanAmount;
        private BigDecimal interestRate;
        private Integer tenureMonths;
        private BigDecimal emiAmount;
        private BigDecimal outstandingAmount;
        private String status;
        private LocalDate applicationDate;
        private LocalDate disbursementDate;
    }

    // ============ KYC DTOs ============

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KycDocumentUploadRequest {
        private Long customerId;
        private String documentType;
        private String documentNumber;
        private String documentUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KycDocumentResponse {
        private Long kycId;
        private String documentType;
        private String documentNumber;
        private String documentUrl;
        private String verificationStatus;
        private LocalDateTime uploadDate;
        private LocalDateTime verificationDate;
        private String rejectionReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KycVerificationRequest {
        private Long kycId;
        private String status;
        private String rejectionReason;
        private Long employeeId;
    }

    // ============ Notification DTOs ============

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponse {
        private Long notificationId;
        private String title;
        private String message;
        private String notificationType;
        private Boolean isRead;
        private LocalDateTime createdAt;
    }

    // ============ Generic Response ============

    @Data
    @NoArgsConstructor
    public static class ApiResponse<T> {

        private boolean success;
        private String message;
        private T data;
        private String error;

        public ApiResponse(boolean success, String message, T data, String error) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.error = error;
        }

        public static <T> ApiResponse<T> success(String message, T data) {
            return new ApiResponse<>(true, message, data, null);
        }

        public static <T> ApiResponse<T> error(String message, String error) {
            return new ApiResponse<>(false, message, null, error);
        }
    }
}
