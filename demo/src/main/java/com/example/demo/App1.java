package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.demo")
@EnableScheduling
public class App1 {
    public static void main(String[] args) {
        SpringApplication.run(App1.class, args);
    }
}
