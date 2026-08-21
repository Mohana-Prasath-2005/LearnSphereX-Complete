package com.learnspherex.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.learnspherex.notification.entity.Notification;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findByReadStatus(boolean readStatus);

    boolean existsByUserIdAndTypeAndReadStatus(
            Long userId,
            String type,
            boolean readStatus);
}