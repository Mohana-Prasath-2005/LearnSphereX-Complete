package com.learnspherex.notification.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.learnspherex.auth.User;
import com.learnspherex.auth.UserRepository;
import com.learnspherex.notification.email.EmailNotificationService;
import com.learnspherex.notification.entity.Notification;
import com.learnspherex.notification.service.NotificationService;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;

    private final EmailNotificationService emailNotificationService;

    private final UserRepository userRepository;


    public NotificationEventListener(
            NotificationService notificationService,
            EmailNotificationService emailNotificationService,
            UserRepository userRepository) {

        this.notificationService =
                notificationService;

        this.emailNotificationService =
                emailNotificationService;

        this.userRepository = userRepository;
    }


    @EventListener
    public void handleNotificationEvent(
            NotificationEvent event) {

        // ==========================================
        // SAVE APPLICATION NOTIFICATION
        // ==========================================

        if (event.getUserId() != null
                && !notificationService
                        .hasUnreadNotification(
                                event.getUserId(),
                                event.getType())) {

            Notification notification =
                    new Notification();

            notification.setUserId(
                    event.getUserId());

            notification.setTitle(
                    event.getTitle());

            notification.setMessage(
                    event.getMessage());

            notification.setType(
                    event.getType());

            notification.setReadStatus(false);

            notificationService
                    .createNotification(notification);
        }


        // ==========================================
        // SEND EMAIL
        // ==========================================

        // Callers rarely have the recipient's email handy (they only know the
        // userId), so resolve it here rather than requiring every publisher to
        // look it up. This is what made email delivery dead everywhere before.
        String recipientEmail = event.getEmail();

        if ((recipientEmail == null || recipientEmail.isBlank())
                && event.getUserId() != null) {

            recipientEmail = userRepository.findById(event.getUserId())
                    .map(User::getEmail)
                    .orElse(null);
        }

        if (recipientEmail != null && !recipientEmail.isBlank()) {

            try {

                emailNotificationService
                        .sendNotificationEmail(
                                recipientEmail,
                                event.getTitle(),
                                event.getMessage());

            } catch (Exception ex) {

                // A down/misconfigured mail server (the default in this app)
                // must not fail the action that triggered the notification.
                log.warn("Failed to send notification email to {}: {}",
                        recipientEmail, ex.getMessage());
            }
        }
    }
}