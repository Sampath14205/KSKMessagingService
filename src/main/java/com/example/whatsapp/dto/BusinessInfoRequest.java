package com.example.whatsapp.dto;

import jakarta.validation.constraints.NotBlank;

public record BusinessInfoRequest(
        @NotBlank String businessId,
        @NotBlank String businessName,
        String legalName,
        String email,
        String phone,
        String website,
        String address
) {}
