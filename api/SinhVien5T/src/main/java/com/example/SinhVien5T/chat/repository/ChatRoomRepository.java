package com.example.SinhVien5T.chat.repository;

import com.example.SinhVien5T.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @EntityGraph(attributePaths = {"user", "user.detail"})
    Optional<ChatRoom> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "user.detail"})
    Optional<ChatRoom> findByPublicId(String publicId);

    boolean existsByPublicIdAndUserId(String publicId, Long userId);

    @EntityGraph(attributePaths = {"user", "user.detail"})
    @Query("""
            select r
            from ChatRoom r
            left join r.user u
            left join u.detail d
            where (:keyword is null
                or lower(u.email) like :keyword
                or lower(d.fullName) like :keyword
                or lower(d.studentCode) like :keyword)
            order by coalesce(r.lastMessageAt, r.createdAt) desc, r.id desc
            """)
    Page<ChatRoom> searchForAdmin(@Param("keyword") String keyword, Pageable pageable);
}
