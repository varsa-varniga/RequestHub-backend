package com.varniga.requestmanagement.controller;

import com.varniga.requestmanagement.entity.Notification;
import com.varniga.requestmanagement.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@CrossOrigin
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Get all notifications for a user
    @GetMapping("/{userId}")
    public List<Notification> getNotifications(@PathVariable Long userId) {
        return notificationService.getUserNotifications(userId);
    }

    // Get unread count
    @GetMapping("/{userId}/unread-count")
    public long getUnreadCount(@PathVariable Long userId) {
        return notificationService.getUnreadCount(userId);
    }

    // Mark one notification as read
    @PutMapping("/{notificationId}/read")
    public String markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return "Notification marked as read";
    }

    // Mark all as read
    @PutMapping("/{userId}/read-all")
    public String markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return "All notifications marked as read";
    }
}