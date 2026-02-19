package com.finsecure.dto;

import com.finsecure.entity.Account.AccountStatus;
import com.finsecure.entity.Account.AccountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private BigDecimal minimumBalance;
    private String currency;
    private AccountStatus status;
    private String ifscCode;
    private String branchName;
    private LocalDateTime createdAt;
}
