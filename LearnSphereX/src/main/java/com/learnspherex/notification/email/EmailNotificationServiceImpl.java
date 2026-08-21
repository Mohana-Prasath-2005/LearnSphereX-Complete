package com.learnspherex.notification.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationServiceImpl
        implements EmailNotificationService {

    private final JavaMailSender mailSender;

    public EmailNotificationServiceImpl(
            JavaMailSender mailSender) {

        this.mailSender = mailSender;
    }

    @Override
    public void sendNotificationEmail(
            String to,
            String subject,
            String message) {

        SimpleMailMessage mail =
                new SimpleMailMessage();

        mail.setTo(to);

        mail.setSubject(subject);

        mail.setText(message);

        mailSender.send(mail);
    }
}