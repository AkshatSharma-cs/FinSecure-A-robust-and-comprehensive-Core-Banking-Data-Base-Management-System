package com.finsecure.service;

import com.finsecure.entity.Notification;
import com.finsecure.entity.Transaction;
import com.finsecure.entity.User;
import com.finsecure.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendTransactionNotification(User user, Transaction transaction) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Transaction Completed");
        notification.setMessage(
            "Your " + transaction.getTransactionType() +
            " of " + transaction.getAmount() +
            " was successful. Ref: " + transaction.getReferenceNumber()
        );
        notification.setNotificationType(Notification.NotificationType.TRANSACTION);

        notificationRepository.save(notification);
    }
}
