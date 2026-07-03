package com.example.SinhVien5T.chat.service;

import com.example.SinhVien5T.chat.dto.ChatRealtimePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RealtimeChatPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishToRoom(String roomPublicId, ChatRealtimePayload payload) {
        messagingTemplate.convertAndSend("/topic/chat/rooms/" + roomPublicId, payload);
    }

    public void publishToAdmins(ChatRealtimePayload payload) {
        messagingTemplate.convertAndSend("/topic/chat/admin", payload);
    }
}
