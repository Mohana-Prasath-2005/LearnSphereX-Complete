package com.learnspherex.notification.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.learnspherex.notification.entity.Notification;

public interface NotificationService {

    Notification createNotification(
            Notification notification);

    List<Notification> getAllNotifications();

    Notification getNotificationById(Long id, Authentication authentication);

    List<Notification> getNotificationsByUserId(
            Long userId, Authentication authentication);

    List<Notification> getUnreadNotifications();

    Notification updateNotification(
            Long id,
            Notification notification,
            Authentication authentication);

    void deleteNotification(Long id, Authentication authentication);

    boolean hasUnreadNotification(
            Long userId,
            String type);
}