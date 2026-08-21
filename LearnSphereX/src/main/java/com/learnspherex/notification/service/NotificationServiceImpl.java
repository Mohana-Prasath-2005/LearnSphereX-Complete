package com.learnspherex.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.learnspherex.notification.entity.Notification;
import com.learnspherex.notification.repository.NotificationRepository;
import com.learnspherex.security.CurrentUserService;

@Service
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            CurrentUserService currentUserService) {

        this.notificationRepository =
                notificationRepository;
        this.currentUserService = currentUserService;
    }


    @Override
    public Notification createNotification(
            Notification notification) {

        if (notification.getUserId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User ID is required for notification"
            );
        }

        if (notification.getCreatedAt() == null) {

            notification.setCreatedAt(
                    LocalDateTime.now());
        }

        return notificationRepository.save(notification);
    }


    @Override
    public List<Notification> getAllNotifications() {

        return notificationRepository.findAll();
    }


    private Notification findEntity(Long id) {

        return notificationRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Notification not found with id: "
                                        + id));
    }


    @Override
    public Notification getNotificationById(
            Long id, Authentication authentication) {

        Notification notification = findEntity(id);
        currentUserService.assertOwnerOrRole(authentication, notification.getUserId(), "ADMIN");
        return notification;
    }


    @Override
    public List<Notification> getNotificationsByUserId(
            Long userId, Authentication authentication) {

        currentUserService.assertOwnerOrRole(authentication, userId, "ADMIN");

        return notificationRepository
                .findByUserId(userId);
    }


    @Override
    public List<Notification> getUnreadNotifications() {

        return notificationRepository
                .findByReadStatus(false);
    }


    @Override
    public Notification updateNotification(
            Long id,
            Notification notification,
            Authentication authentication) {

        Notification existing = findEntity(id);
        currentUserService.assertOwnerOrRole(authentication, existing.getUserId(), "ADMIN");

        // Only overwrite fields the caller actually provided (e.g. the "mark as
        // read" action only ever sends {readStatus:true}); blindly copying every
        // field from the request used to null out title/message/type/userId.
        if (notification.getUserId() != null) {
            existing.setUserId(notification.getUserId());
        }

        if (notification.getTitle() != null) {
            existing.setTitle(notification.getTitle());
        }

        if (notification.getMessage() != null) {
            existing.setMessage(notification.getMessage());
        }

        if (notification.getType() != null) {
            existing.setType(notification.getType());
        }

        existing.setReadStatus(
                notification.isReadStatus());

        return notificationRepository.save(existing);
    }


    @Override
    public void deleteNotification(Long id, Authentication authentication) {

        Notification existing = findEntity(id);
        currentUserService.assertOwnerOrRole(authentication, existing.getUserId(), "ADMIN");

        notificationRepository.delete(existing);
    }


    @Override
    public boolean hasUnreadNotification(
            Long userId,
            String type) {

        return notificationRepository
                .existsByUserIdAndTypeAndReadStatus(
                        userId,
                        type,
                        false);
    }
}