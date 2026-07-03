package com.example.SinhVien5T.notification.repository;

import com.example.SinhVien5T.notification.entity.Notification;
import com.example.SinhVien5T.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByPublicId(String publicId);

    Optional<Notification> findByPublicIdAndRecipientId(String publicId, Long recipientId);

    List<Notification> findByRecipientIdAndReadFalse(Long recipientId);

    long countByRecipientIdAndReadFalse(Long recipientId);

    @EntityGraph(attributePaths = {"recipient"})
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    @EntityGraph(attributePaths = {"recipient"})
    Page<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(
            Long recipientId,
            NotificationType type,
            Pageable pageable
    );

    boolean existsByRecipientIdAndTypeAndRelatedEntityTypeAndRelatedEntityIdAndCreatedAtBetween(
            Long recipientId,
            NotificationType type,
            String relatedEntityType,
            String relatedEntityId,
            LocalDateTime from,
            LocalDateTime to
    );
}
