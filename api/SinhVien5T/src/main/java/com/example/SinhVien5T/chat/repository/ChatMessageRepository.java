package com.example.SinhVien5T.chat.repository;

import com.example.SinhVien5T.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = {"room", "sender", "sender.detail"})
    Page<ChatMessage> findByRoomPublicIdOrderByCreatedAtDescIdDesc(String roomPublicId, Pageable pageable);
}
