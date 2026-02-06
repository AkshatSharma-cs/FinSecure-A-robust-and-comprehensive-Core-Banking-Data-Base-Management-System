package com.finsecure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Customer", indexes = {
    @Index(name = "idx_kyc_status", columnList = "kyc_status"),
    @Index(name = "idx_account_type", columnList = "account_type"),
    @Index(name = "idx_customer_kyc_lookup", columnList = "customer_id,kyc_status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(unique = true, nullable = false, length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country = "India";

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status")
    private KycStatus kycStatus = KycStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "guardian_customer_id")
    private Customer guardian;

    @OneToMany(mappedBy = "guardian")
    private List<Customer> dependents;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Account> accounts;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Card> cards;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Loan> loans;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<KycDocument> kycDocuments;

    public enum AccountType {
        STUDENT,
        ADULT,
        MINOR
    }

    public enum KycStatus {
        PENDING,
        APPROVED,
        REJECTED,
        INCOMPLETE
    }
}
