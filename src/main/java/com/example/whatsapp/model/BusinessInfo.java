package com.example.whatsapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("business_info")
public record BusinessInfo(
        @Id String id,
        @Indexed(unique = true) String businessId,
        String businessName,
        String legalName,
        String email,
        String phone,
        String website,
        String address,
        Instant createdAt,
        Instant updatedAt
) {}
