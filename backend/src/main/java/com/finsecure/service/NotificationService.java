package com.finsecure.service;

import com.finsecure.dto.NotificationResponse;
import com.finsecure.entity.Notification;
import com.finsecure.entity.Notification.NotificationType;
import com.finsecure.entity.User;
import com.finsecure.repository.NotificationRepository;
import com.finsecure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createNotification(Long userId, NotificationType type, String title, String message, String referenceId, String referenceType) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot create notification: user {} not found", userId);
            return;
        }

        Notification notification = Notification.builder()
            .user(user)
            .type(type)
            .title(title)
            .message(message)
            .referenceId(referenceId)
            .referenceType(referenceType)
            .build();

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    public void sendTransactionNotification(Long userId, String accountNumber, String amount, String type) {
        createNotification(userId, NotificationType.TRANSACTION,
            "Transaction Alert",
            String.format("A %s of Rs. %s has been processed on account %s", type, amount, accountNumber),
            accountNumber, "ACCOUNT");
    }

    public void sendLoanNotification(Long userId, String loanNumber, String status) {
        createNotification(userId, NotificationType.LOAN,
            "Loan Status Update",
            String.format("Your loan application %s has been %s", loanNumber, status),
            loanNumber, "LOAN");
    }

    public void sendKycNotification(Long userId, String status) {
        createNotification(userId, NotificationType.KYC,
            "KYC Status Update",
            String.format("Your KYC verification has been %s", status),
            null, null);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .type(notification.getType())
            .title(notification.getTitle())
            .message(notification.getMessage())
            .isRead(notification.getIsRead())
            .referenceId(notification.getReferenceId())
            .referenceType(notification.getReferenceType())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}
