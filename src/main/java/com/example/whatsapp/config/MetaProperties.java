package com.example.whatsapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "meta.whatsapp")
public record MetaProperties(
        String graphUrl,
        String graphVersion,
        String phoneNumberId,
        String accessToken,
        String verifyToken,
        String appSecret,
        long bulkDelayMs,
        int maxBatchSize
) {}
