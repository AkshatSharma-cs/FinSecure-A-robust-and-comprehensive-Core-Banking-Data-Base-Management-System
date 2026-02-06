package com.finsecure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Audit_Log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    private String entityType;
    private Long entityId;
    private String description;
    private String ipAddress;
    private String userAgent;

    private LocalDateTime timestamp = LocalDateTime.now();

    public enum ActionType {
        LOGIN, LOGOUT, FAILED_LOGIN, KYC_UPLOAD, KYC_APPROVE, KYC_REJECT,
        TRANSACTION, CARD_BLOCK, CARD_UNBLOCK, CARD_REPLACE,
        PASSWORD_CHANGE, PROFILE_UPDATE, ACCOUNT_CREATE, LOAN_APPLY, INVESTMENT_CREATE
    }
}
