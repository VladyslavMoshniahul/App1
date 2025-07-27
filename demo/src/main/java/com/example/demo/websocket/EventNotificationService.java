package com.example.demo.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.javaSrc.users.UserRepository;

@Service
public class EventNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UserRepository userRepository;
    
    public void notifyUserAboutEvent(Long userId, String message) {
        String username = userRepository.findEmailById(userId);
        messagingTemplate.convertAndSendToUser(username, "/queue/private", message);
    }
}
