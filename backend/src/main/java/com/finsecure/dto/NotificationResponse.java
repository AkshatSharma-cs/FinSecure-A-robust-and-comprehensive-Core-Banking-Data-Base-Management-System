package com.finsecure.dto;

import com.finsecure.entity.Notification.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean isRead;
    private String referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
}
