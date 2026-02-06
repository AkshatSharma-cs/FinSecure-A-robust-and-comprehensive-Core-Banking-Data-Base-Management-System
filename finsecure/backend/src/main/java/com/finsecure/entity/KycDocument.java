package com.finsecure.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "KYC_Document")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long kycId;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    private String documentNumber;
    private String documentUrl;

    private LocalDateTime uploadDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @ManyToOne
    private Employee verifiedBy;

    private LocalDateTime verificationDate;
    private String rejectionReason;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum DocumentType {
        AADHAAR, PAN, PASSPORT, DRIVING_LICENSE, VOTER_ID, ADDRESS_PROOF, PHOTO
    }

    public enum VerificationStatus {
        PENDING, APPROVED, REJECTED
    }
}
