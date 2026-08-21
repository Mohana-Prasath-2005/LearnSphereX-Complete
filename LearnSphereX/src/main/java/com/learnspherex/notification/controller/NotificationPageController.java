package com.learnspherex.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.learnspherex.auth.User;
import com.learnspherex.auth.UserRepository;
import com.learnspherex.notification.entity.Notification;
import com.learnspherex.notification.service.NotificationService;

@Controller
public class NotificationPageController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationPageController(
            NotificationService notificationService,
            UserRepository userRepository) {

        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/notifications")
    public String showNotifications(
            Authentication authentication,
            Model model) {

        String username = authentication.getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Logged-in user not found"));

        model.addAttribute(
                "notifications",
                notificationService
                        .getNotificationsByUserId(user.getId(), authentication));

        model.addAttribute(
                "username",
                user.getUsername());

        return "notifications";
    }

    @PostMapping("/notifications/{id}/read")
    @ResponseBody
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        Notification patch = new Notification();
        patch.setReadStatus(true);

        notificationService.updateNotification(id, patch, authentication);

        return ResponseEntity.noContent().build();
    }
}