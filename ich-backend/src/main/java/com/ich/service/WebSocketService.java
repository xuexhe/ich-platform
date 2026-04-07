// src/main/java/com/ich/service/WebSocketService.java
package com.ich.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 发送成就通知给指定用户
     * @param userId 用户ID
     * @param achievement 成就信息
     */
    public void sendAchievementNotification(Long userId, Map<String, Object> achievement) {
        String destination = "/topic/achievement/" + userId;
        messagingTemplate.convertAndSend(destination, achievement);
    }

    /**
     * 发送通用通知
     * @param userId 用户ID
     * @param message 消息内容
     */
    public void sendNotification(Long userId, Map<String, Object> message) {
        String destination = "/topic/notification/" + userId;
        messagingTemplate.convertAndSend(destination, message);
    }
}