package com.finsecure.dto;

import com.finsecure.entity.Loan.LoanStatus;
import com.finsecure.entity.Loan.LoanType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanResponse {

    private Long id;
    private String loanNumber;
    private LoanType loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal emiAmount;
    private BigDecimal outstandingAmount;
    private BigDecimal totalInterest;
    private LoanStatus status;
    private LocalDate disbursementDate;
    private LocalDate nextEmiDate;
    private String purpose;
    private String rejectionReason;
    private LocalDateTime createdAt;
}
