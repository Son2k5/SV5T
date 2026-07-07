package com.example.SinhVien5T.notification.service;

import com.example.SinhVien5T.notification.dto.RealtimeNotificationPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RealtimeNotificationPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.notification.realtime.redis-enabled:false}")
    private boolean redisRealtimeEnabled;

    public RealtimeNotificationPublisher(
            @Qualifier("stringRedisTemplate") StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(String userPublicId, RealtimeNotificationPayload payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            if (redisRealtimeEnabled) {
                stringRedisTemplate.convertAndSend(channel(userPublicId), payloadJson);
                return;
            }

            sendDirectly(userPublicId, payloadJson);
        } catch (Exception ex) {
            log.warn("Could not publish realtime notification for userPublicId={}", userPublicId, ex);
        }
    }

    private String channel(String userPublicId) {
        return "notifications:" + userPublicId;
    }

    private void sendDirectly(String userPublicId, String payloadJson) {
        messagingTemplate.convertAndSend("/topic/notifications/" + userPublicId, payloadJson);
    }
}
