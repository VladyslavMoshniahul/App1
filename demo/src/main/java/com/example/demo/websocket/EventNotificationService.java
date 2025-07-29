package com.example.demo.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.javaSrc.peoples.PeopleRepository;

@Service
public class EventNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private PeopleRepository userRepository;
    
    public void notifyUserAboutEvent(Long userId, String message) {
        String username = userRepository.findEmailById(userId);
        messagingTemplate.convertAndSendToUser(username, "/queue/private", message);
    }
}
