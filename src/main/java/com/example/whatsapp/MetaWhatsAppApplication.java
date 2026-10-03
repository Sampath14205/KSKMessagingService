package com.example.whatsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MetaWhatsAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetaWhatsAppApplication.class, args);
    }
}
