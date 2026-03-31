package com.varniga.requestmanagement.service;

import com.varniga.requestmanagement.entity.Notification;
import com.varniga.requestmanagement.enums.NotificationType;
import com.varniga.requestmanagement.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Create Notification
    public void createNotification(Long userId, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .userId(userId)
                .message(message)
                .type(type)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    // Get all notifications for a user
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Get unread count
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // Mark one as read
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    // Mark all as read (optional but good)
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        for (Notification n : notifications) {
            n.setIsRead(true);
        }

        notificationRepository.saveAll(notifications);
    }
}