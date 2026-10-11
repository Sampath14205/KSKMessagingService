package com.example.whatsapp.model;

import java.time.Instant;

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
