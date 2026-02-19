package com.finsecure.dto;

import com.finsecure.entity.Account.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateAccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    private String branchName;
    private String ifscCode;
}
