package com.finsecure.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "OTP")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long otpId;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    private String otpCode;

    @Enumerated(EnumType.STRING)
    private OtpType otpType;

    private String email;
    private Boolean isVerified = false;
    private LocalDateTime expiresAt;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum OtpType {
        LOGIN, TRANSACTION, CARD_BLOCK, PASSWORD_RESET, PROFILE_UPDATE, KYC_CONFIRMATION
    }
}
