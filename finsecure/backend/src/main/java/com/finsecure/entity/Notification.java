package com.finsecure.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "Notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    private String title;
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    private Boolean isRead = false;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum NotificationType {
        INFO, SUCCESS, WARNING, ALERT, KYC_STATUS, TRANSACTION, LOAN_STATUS, PAYMENT_DUE
    }
}