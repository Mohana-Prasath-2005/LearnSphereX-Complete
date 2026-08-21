package com.learnspherex.notification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnspherex.notification.entity.Notification;
import com.learnspherex.notification.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;


    public NotificationController(
            NotificationService notificationService) {

        this.notificationService =
                notificationService;
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Notification>
    createNotification(
            @RequestBody Notification notification) {

        Notification created =
                notificationService
                        .createNotification(notification);

        return new ResponseEntity<>(
                created,
                HttpStatus.CREATED);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Notification>>
    getAllNotifications() {

        return ResponseEntity.ok(
                notificationService
                        .getAllNotifications());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Notification>
    getNotificationById(
            @PathVariable Long id, Authentication authentication) {

        return ResponseEntity.ok(
                notificationService
                        .getNotificationById(id, authentication));
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>>
    getNotificationsByUserId(
            @PathVariable Long userId, Authentication authentication) {

        return ResponseEntity.ok(
                notificationService
                        .getNotificationsByUserId(userId, authentication));
    }


    @GetMapping("/unread")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Notification>>
    getUnreadNotifications() {

        return ResponseEntity.ok(
                notificationService
                        .getUnreadNotifications());
    }


    @PutMapping("/{id}")
    public ResponseEntity<Notification>
    updateNotification(
            @PathVariable Long id,
            @RequestBody Notification notification,
            Authentication authentication) {

        return ResponseEntity.ok(
                notificationService
                        .updateNotification(
                                id,
                                notification,
                                authentication));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteNotification(
            @PathVariable Long id, Authentication authentication) {

        notificationService.deleteNotification(id, authentication);

        return ResponseEntity.noContent().build();
    }
}