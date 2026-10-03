package com.example.whatsapp.dto;

import jakarta.validation.constraints.NotBlank;

public record SendTextRequest(
        @NotBlank String to,
        @NotBlank String body,
        Boolean previewUrl
) {}
