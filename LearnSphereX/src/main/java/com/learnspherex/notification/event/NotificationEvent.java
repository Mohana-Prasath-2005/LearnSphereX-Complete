package com.learnspherex.notification.event;

public class NotificationEvent {

    private final Long userId;

    private final String email;

    private final String title;

    private final String message;

    private final String type;


    public NotificationEvent(
            Long userId,
            String email,
            String title,
            String message,
            String type) {

        this.userId = userId;
        this.email = email;
        this.title = title;
        this.message = message;
        this.type = type;
    }


    public Long getUserId() {
        return userId;
    }


    public String getEmail() {
        return email;
    }


    public String getTitle() {
        return title;
    }


    public String getMessage() {
        return message;
    }


    public String getType() {
        return type;
    }
}