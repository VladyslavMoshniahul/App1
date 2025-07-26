package com.example.demo.javaSrc.events;

import java.time.LocalDateTime;

public record EventCreateRequest(
        String title,
        String content,
        String locationORlink,
        LocalDateTime startEvent,
        int duration,
        Event.EventType eventType,
        String className) {

}
