package com.finsecure.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardResponse {

    private CustomerProfileResponse profile;
    private BigDecimal totalBalance;
    private Integer totalAccounts;
    private Integer activeLoans;
    private Integer activeCards;
    private Long unreadNotifications;
    private List<AccountResponse> accounts;
    private List<TransactionResponse> recentTransactions;
    private List<NotificationResponse> notifications;
}
