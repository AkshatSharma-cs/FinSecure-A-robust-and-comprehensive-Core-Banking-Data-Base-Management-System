package com.finsecure.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Loan")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal emiAmount;
    private BigDecimal outstandingAmount;

    @Enumerated(EnumType.STRING)
    private Status status = Status.APPLIED;

    private LocalDate applicationDate;
    private LocalDate approvalDate;
    private LocalDate disbursementDate;
    private LocalDate closureDate;

    @ManyToOne
    private Employee approvedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum LoanType { PERSONAL, HOME, AUTO, EDUCATION, BUSINESS }
    public enum Status { APPLIED, APPROVED, DISBURSED, ACTIVE, CLOSED, REJECTED, DEFAULTED }
}
