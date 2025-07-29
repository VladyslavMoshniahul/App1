package com.example.demo.websocket;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            @SuppressWarnings("null") ServerHttpRequest request,
            @SuppressWarnings("null") ServerHttpResponse response,
            @SuppressWarnings("null") WebSocketHandler wsHandler,
            @SuppressWarnings("null") Map<String, Object> attributes) {

        System.out.println("[Handshake] Початок перевірки");

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            System.out.println("[Handshake] Отримано HttpServletRequest");

            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {

                for (Cookie cookie : cookies) {
                    System.out.println("[Handshake] Перевірка cookie: " + cookie.getName());

                    if ("JSESSIONID".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        System.out.println("[Handshake] Знайдено JSESSIONID cookie: " + token);

                        attributes.put("jwt", token);

                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(
            @SuppressWarnings("null") ServerHttpRequest request,
            @SuppressWarnings("null") ServerHttpResponse response,
            @SuppressWarnings("null") WebSocketHandler wsHandler,
            @SuppressWarnings("null") Exception exception) {
    }

}
