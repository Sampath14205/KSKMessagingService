package com.example.whatsapp.dto;

import jakarta.validation.constraints.NotBlank;

/** Request for sending image/video media by a public HTTPS link or a Meta media ID.
 * Supply exactly one of link or mediaId.
 */
public record SendMediaRequest(
        @NotBlank String to,
        String link,
        String mediaId,
        String caption
) {}
