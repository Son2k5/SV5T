package com.example.SinhVien5T.notification.service;

import com.example.SinhVien5T.notification.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final NotificationService notificationService;

    public void publishAfterCommit(NotificationEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            notificationService.send(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationService.send(event);
            }
        });
    }
}
