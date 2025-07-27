package com.example.demo.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(@SuppressWarnings("null") MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); 
        config.setApplicationDestinationPrefixes("/app"); 
    }

    @Override
    public void registerStompEndpoints(@SuppressWarnings("null") StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .addInterceptors(new WebSocketAuthInterceptor()) 
                .setAllowedOriginPatterns("https://localhost:8443/*")
                .withSockJS(); 
    }
}
