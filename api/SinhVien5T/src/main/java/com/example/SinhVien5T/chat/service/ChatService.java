package com.example.SinhVien5T.chat.service;

import com.example.SinhVien5T.admin.dto.PageResponse;
import com.example.SinhVien5T.chat.dto.ChatMessageDto;
import com.example.SinhVien5T.chat.dto.ChatRealtimePayload;
import com.example.SinhVien5T.chat.dto.ChatRoomDto;
import com.example.SinhVien5T.chat.dto.ChatSendMessageRequest;
import com.example.SinhVien5T.chat.entity.ChatMessage;
import com.example.SinhVien5T.chat.entity.ChatRoom;
import com.example.SinhVien5T.chat.repository.ChatMessageRepository;
import com.example.SinhVien5T.chat.repository.ChatRoomRepository;
import com.example.SinhVien5T.common.exception.ResourceNotFoundException;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.entity.UserDetail;
import com.example.SinhVien5T.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final RealtimeChatPublisher realtimeChatPublisher;

    @Transactional
    public ChatRoomDto getOrCreateMyRoom(CustomUserDetails currentUser) {
        ChatRoom room = getOrCreateRoom(currentUser.getId());
        return toRoomDto(room);
    }

    @Transactional
    public PageResponse<ChatMessageDto> getMyMessages(CustomUserDetails currentUser, int page, int size) {
        ChatRoom room = getOrCreateRoom(currentUser.getId());
        markReadForUser(room);
        return getMessages(room, currentUser, page, size);
    }

    @Transactional
    public ChatRoomDto markMyRoomRead(CustomUserDetails currentUser) {
        ChatRoom room = getOrCreateRoom(currentUser.getId());
        markReadForUser(room);
        ChatRoomDto roomDto = toRoomDto(room);
        realtimeChatPublisher.publishToRoom(room.getPublicId(), ChatRealtimePayload.roomUpdated(roomDto));
        return roomDto;
    }

    @Transactional
    public ChatMessageDto sendMyMessage(CustomUserDetails currentUser, ChatSendMessageRequest request) {
        ChatRoom room = getOrCreateRoom(currentUser.getId());
        User sender = getUser(currentUser.getId());
        ChatMessage message = createMessage(room, sender, request.content().trim());
        LocalDateTime sentAt = LocalDateTime.now();
        message.setCreatedAt(sentAt);

        room.setLastMessage(message.getContent());
        room.setLastMessageAt(sentAt);
        room.setUnreadByUser(0);
        room.setUnreadByAdmin(room.getUnreadByAdmin() + 1);

        chatMessageRepository.save(message);
        ChatRoomDto roomDto = toRoomDto(room);
        ChatMessageDto messageDto = toMessageDto(message, currentUser.getId());
        ChatRealtimePayload payload = ChatRealtimePayload.message(roomDto, messageDto);

        realtimeChatPublisher.publishToRoom(room.getPublicId(), payload);
        realtimeChatPublisher.publishToAdmins(payload);

        return messageDto;
    }

    @Transactional(readOnly = true)
    public PageResponse<ChatRoomDto> getAdminRooms(String keyword, int page, int size) {
        String normalizedKeyword = normalizeKeyword(keyword);
        Page<ChatRoom> rooms = chatRoomRepository.searchForAdmin(normalizedKeyword, PageRequest.of(page, size));
        return toPageResponse(rooms.map(this::toRoomDto));
    }

    @Transactional
    public PageResponse<ChatMessageDto> getAdminMessages(
            String roomPublicId,
            CustomUserDetails currentUser,
            int page,
            int size
    ) {
        ChatRoom room = getRoom(roomPublicId);
        markReadForAdmin(room);
        ChatRoomDto roomDto = toRoomDto(room);
        realtimeChatPublisher.publishToAdmins(ChatRealtimePayload.roomUpdated(roomDto));
        realtimeChatPublisher.publishToRoom(room.getPublicId(), ChatRealtimePayload.roomUpdated(roomDto));
        return getMessages(room, currentUser, page, size);
    }

    @Transactional
    public ChatRoomDto markAdminRoomRead(String roomPublicId) {
        ChatRoom room = getRoom(roomPublicId);
        markReadForAdmin(room);
        ChatRoomDto roomDto = toRoomDto(room);
        realtimeChatPublisher.publishToAdmins(ChatRealtimePayload.roomUpdated(roomDto));
        realtimeChatPublisher.publishToRoom(room.getPublicId(), ChatRealtimePayload.roomUpdated(roomDto));
        return roomDto;
    }

    @Transactional
    public ChatMessageDto sendAdminMessage(
            String roomPublicId,
            CustomUserDetails currentUser,
            ChatSendMessageRequest request
    ) {
        ChatRoom room = getRoom(roomPublicId);
        User sender = getUser(currentUser.getId());
        ChatMessage message = createMessage(room, sender, request.content().trim());
        LocalDateTime sentAt = LocalDateTime.now();
        message.setCreatedAt(sentAt);

        room.setLastMessage(message.getContent());
        room.setLastMessageAt(sentAt);
        room.setUnreadByUser(room.getUnreadByUser() + 1);
        room.setUnreadByAdmin(0);

        chatMessageRepository.save(message);
        ChatRoomDto roomDto = toRoomDto(room);
        ChatMessageDto messageDto = toMessageDto(message, currentUser.getId());
        ChatRealtimePayload payload = ChatRealtimePayload.message(roomDto, messageDto);

        realtimeChatPublisher.publishToRoom(room.getPublicId(), payload);
        realtimeChatPublisher.publishToAdmins(payload);

        return messageDto;
    }

    private ChatRoom getOrCreateRoom(Long userId) {
        return chatRoomRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = getUser(userId);
                    return chatRoomRepository.save(ChatRoom.builder()
                            .user(user)
                            .lastMessage(null)
                            .build());
                });
    }

    private ChatRoom getRoom(String roomPublicId) {
        return chatRoomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phong chat"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung"));
    }

    private ChatMessage createMessage(ChatRoom room, User sender, String content) {
        return ChatMessage.builder()
                .room(room)
                .sender(sender)
                .senderRole(sender.getRole())
                .content(content)
                .build();
    }

    private PageResponse<ChatMessageDto> getMessages(
            ChatRoom room,
            CustomUserDetails currentUser,
            int page,
            int size
    ) {
        Page<ChatMessage> messages = chatMessageRepository.findByRoomPublicIdOrderByCreatedAtDescIdDesc(
                room.getPublicId(),
                PageRequest.of(page, size)
        );
        return toPageResponse(messages.map(message -> toMessageDto(message, currentUser.getId())));
    }

    private void markReadForUser(ChatRoom room) {
        if (room.getUnreadByUser() > 0) {
            room.setUnreadByUser(0);
        }
    }

    private void markReadForAdmin(ChatRoom room) {
        if (room.getUnreadByAdmin() > 0) {
            room.setUnreadByAdmin(0);
        }
    }

    private ChatRoomDto toRoomDto(ChatRoom room) {
        User user = room.getUser();
        return new ChatRoomDto(
                room.getPublicId(),
                user.getPublicId(),
                displayName(user),
                user.getEmail(),
                user.getAvatar(),
                room.getLastMessage(),
                room.getLastMessageAt(),
                room.getUnreadByUser(),
                room.getUnreadByAdmin(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }

    private ChatMessageDto toMessageDto(ChatMessage message, Long currentUserId) {
        User sender = message.getSender();
        return new ChatMessageDto(
                message.getPublicId(),
                message.getRoom().getPublicId(),
                sender.getPublicId(),
                displayName(sender),
                sender.getEmail(),
                message.getSenderRole(),
                sender.getId().equals(currentUserId),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    private String displayName(User user) {
        UserDetail detail = user.getDetail();
        if (detail != null && detail.getFullName() != null && !detail.getFullName().isBlank()) {
            return detail.getFullName();
        }
        return user.getEmail();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return "%" + keyword.trim().toLowerCase() + "%";
    }

    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
