package com.finsecure.dto;

import com.finsecure.entity.Transaction.TransactionMode;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionRequest {

    @NotBlank(message = "Source account number is required")
    private String fromAccountNumber;

    private String toAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Minimum transaction amount is 1")
    @DecimalMax(value = "1000000.0", message = "Maximum transaction amount is 10,00,000")
    private BigDecimal amount;

    @NotNull(message = "Transaction mode is required")
    private TransactionMode mode;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    private String otpCode;
}
