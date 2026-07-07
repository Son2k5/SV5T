package com.example.SinhVien5T.notification.repository;

import com.example.SinhVien5T.notification.entity.Notification;
import com.example.SinhVien5T.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
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

    @Query("SELECT n.recipient.id FROM Notification n WHERE n.recipient.id IN :recipientIds AND n.type = :type AND n.relatedEntityType = :relatedEntityType AND n.relatedEntityId = :relatedEntityId AND n.createdAt BETWEEN :from AND :to")
    List<Long> findRemindedUserIds(
            @Param("recipientIds") Collection<Long> recipientIds,
            @Param("type") NotificationType type,
            @Param("relatedEntityType") String relatedEntityType,
            @Param("relatedEntityId") String relatedEntityId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
