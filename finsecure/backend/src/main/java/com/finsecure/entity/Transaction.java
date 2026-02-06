package com.finsecure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Transaction", indexes = {
    @Index(name = "idx_account_id", columnList = "account_id"),
    @Index(name = "idx_transaction_date", columnList = "transaction_date"),
    @Index(name = "idx_reference_number", columnList = "reference_number"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_transaction_account_date", columnList = "account_id,transaction_date")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "counterparty_account", length = 20)
    private String counterpartyAccount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "reference_number", unique = true, nullable = false, length = 50)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "otp_verified")
    private Boolean otpVerified = false;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate = LocalDateTime.now();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER_IN,
        TRANSFER_OUT,
        INTEREST_CREDIT,
        FEE_DEBIT,
        LOAN_DISBURSEMENT,
        LOAN_REPAYMENT
    }

    public enum Status {
        PENDING,
        SUCCESS,
        FAILED,
        REVERSED
    }
}
