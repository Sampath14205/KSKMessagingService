package com.example.whatsapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BulkTextRequest(
        @NotEmpty List<@Valid SendTextRequest> messages
) {}
