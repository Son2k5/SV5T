package com.example.SinhVien5T.admin.repository;

import com.example.SinhVien5T.common.entity.AuditLog;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface AdminAuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByEntityAndEntityId(String entity, Long entityId, Pageable pageable);

    @Query("""
           select a
           from AuditLog a
           where (:entity is null or a.entity = :entity)
             and (:action is null or a.action = :action)
             and (:userId is null or a.actorId = :userId)
           order by a.createdAt desc
           """)
    Page<AuditLog> search(@Param("entity") String entity,
                          @Param("action") String action,
                          @Param("userId") Long userId,
                          Pageable pageable);
}