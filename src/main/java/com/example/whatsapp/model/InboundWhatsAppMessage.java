package com.example.whatsapp.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

public record InboundWhatsAppMessage(
        String messageId,
        String from,
        String profileName,
        String type,
        String text,
        String timestamp,
        String phoneNumberId,
        String displayPhoneNumber,
        Instant receivedAt
) {}
