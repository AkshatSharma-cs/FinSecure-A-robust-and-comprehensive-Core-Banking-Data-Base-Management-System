package com.finsecure.dto;

import com.finsecure.entity.Transaction.TransactionMode;
import com.finsecure.entity.Transaction.TransactionStatus;
import com.finsecure.entity.Transaction.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionResponse {

    private Long id;
    private String referenceNumber;
    private String accountNumber;
    private TransactionType type;
    private TransactionMode mode;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private String targetAccountNumber;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
