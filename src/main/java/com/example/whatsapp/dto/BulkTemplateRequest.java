package com.example.whatsapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BulkTemplateRequest(
        @NotEmpty List<@Valid TemplateRequest> messages
) {}
