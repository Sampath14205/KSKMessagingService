package com.example.whatsapp.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record TemplateRequest(
        @NotBlank String to,
        @NotBlank String templateName,
        @NotBlank String languageCode,
        List<String> bodyParameters
) {}
