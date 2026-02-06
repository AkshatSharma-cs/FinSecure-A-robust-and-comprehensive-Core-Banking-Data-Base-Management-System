package com.finsecure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Card", indexes = {
    @Index(name = "idx_card_number", columnList = "card_number"),
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_card_customer_status", columnList = "customer_id,status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cardId;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "card_number", unique = true, nullable = false, length = 19)
    private String cardNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;

    @Column(name = "card_holder_name", nullable = false, length = 100)
    private String cardHolderName;

    @Column(name = "cvv_hash", nullable = false)
    private String cvvHash;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "available_credit", precision = 12, scale = 2)
    private BigDecimal availableCredit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING_ACTIVATION;

    @Column(name = "pin_hash")
    private String pinHash;

    @Column(name = "is_international_enabled")
    private Boolean isInternationalEnabled = false;

    @Column(name = "is_online_enabled")
    private Boolean isOnlineEnabled = true;

    @Column(name = "replacement_requested")
    private Boolean replacementRequested = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum CardType {
        DEBIT,
        CREDIT,
        PREPAID,
        STUDENT,
        COMMERCIAL
    }

    public enum Status {
        ACTIVE,
        BLOCKED,
        EXPIRED,
        REPLACED,
        PENDING_ACTIVATION
    }
}
