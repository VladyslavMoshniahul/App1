package com.example.demo.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

    @MessageMapping("/notify")
    @SendTo("/topic/notifications")
    public String sendNotification(String message) {
        return message;
    }
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendPrivateNotification(String username, String message) {
        messagingTemplate.convertAndSendToUser(username, "/queue/private", message);
    }
}

