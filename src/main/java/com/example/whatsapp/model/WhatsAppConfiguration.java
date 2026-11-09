package com.example.whatsapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("whatsapp_configurations")
public record WhatsAppConfiguration(
        @Id String id,
        @Indexed(unique = true) String businessId,
        String graphUrl,
        String graphVersion,
        @Indexed(unique = true) String phoneNumberId,
        String accessToken,
        String verifyToken,
        String appSecret,
        long bulkDelayMs,
        int maxBatchSize,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
