package com.example.whatsapp.dto;

import jakarta.validation.constraints.NotBlank;

public record WhatsAppConfigurationRequest(
        @NotBlank String businessId,
        String graphUrl,
        String graphVersion,
        @NotBlank String phoneNumberId,
        @NotBlank String accessToken,
        @NotBlank String verifyToken,
        @NotBlank String appSecret,
        Long bulkDelayMs,
        Integer maxBatchSize,
        Boolean active
) {}
