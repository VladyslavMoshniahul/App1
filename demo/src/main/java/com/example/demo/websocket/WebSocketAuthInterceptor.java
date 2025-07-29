package com.example.demo.websocket;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.demo.javaSrc.security.JwtUtils;

import java.util.Map;

public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    private final JwtUtils jwtUtils;

    public WebSocketAuthInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean beforeHandshake(@SuppressWarnings("null") ServerHttpRequest request,
            @SuppressWarnings("null") ServerHttpResponse response,
            @SuppressWarnings("null") WebSocketHandler wsHandler,
            @SuppressWarnings("null") Map<String, Object> attributes) {
        
        return false;
    }

    @Override
    public void afterHandshake(@SuppressWarnings("null") ServerHttpRequest request,
            @SuppressWarnings("null") ServerHttpResponse response,
            @SuppressWarnings("null") WebSocketHandler wsHandler, @SuppressWarnings("null") Exception exception) {
    }
}
