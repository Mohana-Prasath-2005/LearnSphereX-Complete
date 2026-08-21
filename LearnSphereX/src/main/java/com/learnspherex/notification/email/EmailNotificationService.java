package com.learnspherex.notification.email;

public interface EmailNotificationService {

    void sendNotificationEmail(
            String to,
            String subject,
            String message);
}