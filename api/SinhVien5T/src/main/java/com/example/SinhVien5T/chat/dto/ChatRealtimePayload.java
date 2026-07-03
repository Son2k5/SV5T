package com.example.SinhVien5T.chat.dto;

public record ChatRealtimePayload(
        String type,
        ChatRoomDto room,
        ChatMessageDto message
) {
    public static ChatRealtimePayload message(ChatRoomDto room, ChatMessageDto message) {
        return new ChatRealtimePayload("MESSAGE_CREATED", room, message);
    }

    public static ChatRealtimePayload roomUpdated(ChatRoomDto room) {
        return new ChatRealtimePayload("ROOM_UPDATED", room, null);
    }
}
