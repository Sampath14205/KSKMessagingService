package com.example.whatsapp.service;

import com.example.whatsapp.model.WhatsAppConfiguration;
import com.example.whatsapp.dto.TemplateRequest;
import com.example.whatsapp.dto.SendTextRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class MetaWhatsAppService {

    private final RestClient restClient;
    private final DatabaseConfigurationService configurationService;

    public MetaWhatsAppService(RestClient restClient, DatabaseConfigurationService configurationService) {
        this.restClient = restClient;
        this.configurationService = configurationService;
    }

    public Object sendText(SendTextRequest request) {
        Map<String,Object> text = new LinkedHashMap<>();
        text.put("preview_url", Boolean.TRUE.equals(request.previewUrl()));
        text.put("body", request.body());

        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("recipient_type", "individual");
        payload.put("to", normalize(request.to()));
        payload.put("type", "text");
        payload.put("text", text);

        return post(payload,request.businessId());
    }

    public Object sendImage(com.example.whatsapp.dto.SendMediaRequest request) {
        return sendMedia("image", request);
    }

    public Object sendVideo(com.example.whatsapp.dto.SendMediaRequest request) {
        return sendMedia("video", request);
    }

    private Object sendMedia(String mediaType, com.example.whatsapp.dto.SendMediaRequest request) {
        boolean hasLink = request.link() != null && !request.link().isBlank();
        boolean hasMediaId = request.mediaId() != null && !request.mediaId().isBlank();
        if (hasLink == hasMediaId) {
            throw new IllegalArgumentException("Provide exactly one of 'link' or 'mediaId'.");
        }

        Map<String, Object> media = new LinkedHashMap<>();
        if (hasLink) {
            if (!request.link().startsWith("https://")) {
                throw new IllegalArgumentException("Media link must be a publicly accessible HTTPS URL.");
            }
            media.put("link", request.link());
        } else {
            media.put("id", request.mediaId());
        }
        if (request.caption() != null && !request.caption().isBlank()) {
            media.put("caption", request.caption());
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("recipient_type", "individual");
        payload.put("to", normalize(request.to()));
        payload.put("type", mediaType);
        payload.put(mediaType, media);
        return post(payload);
    }

    public Object sendTemplate(TemplateRequest request) {
        Map<String,Object> template = new LinkedHashMap<>();
        template.put("name", request.templateName());
        template.put("language", Map.of("code", request.languageCode()));

        if (request.bodyParameters() != null && !request.bodyParameters().isEmpty()) {
            List<Map<String,String>> parameters = request.bodyParameters().stream()
                    .map(value -> Map.of("type", "text", "text", value))
                    .toList();
            template.put("components", List.of(
                    Map.of("type", "body", "parameters", parameters)
            ));
        }

        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", normalize(request.to()));
        payload.put("type", "template");
        payload.put("template", template);

        return post(payload);
    }

    public CompletableFuture<List<Object>> bulkText(List<SendTextRequest> messages) {
        return CompletableFuture.supplyAsync(() -> {
            validateBatch(messages.size());
            List<Object> results = new ArrayList<>();
            for (SendTextRequest message : messages) {
                try {
                    results.add(sendText(message));
                } catch (Exception e) {
                    results.add(Map.of(
                            "to", message.to(),
                            "success", false,
                            "error", Objects.toString(e.getMessage(), "Unknown error")
                    ));
                }
                sleep();
            }
            return results;
        });
    }

    public CompletableFuture<List<Object>> bulkTemplate(List<TemplateRequest> messages) {
        return CompletableFuture.supplyAsync(() -> {
            validateBatch(messages.size());
            List<Object> results = new ArrayList<>();
            for (TemplateRequest message : messages) {
                try {
                    results.add(sendTemplate(message));
                } catch (Exception e) {
                    results.add(Map.of(
                            "to", message.to(),
                            "success", false,
                            "error", Objects.toString(e.getMessage(), "Unknown error")
                    ));
                }
                sleep();
            }
            return results;
        });
    }

    private Object post(Map<String,Object> payload) {
        return post(payload, "KSK-C7473A34E15B");
    }

    public Object post(Map<String,Object> payload, String businessId) {
        WhatsAppConfiguration config = configurationService.getActiveConfiguration(businessId);
        String url = config.graphUrl() + "/" + config.graphVersion()
                + "/" + config.phoneNumberId() + "/messages";

        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + config.accessToken())
                .body(payload)
                .retrieve()
                .body(Object.class);
    }

    private void validateBatch(int size) {
        int maxBatchSize = configurationService.getActiveConfiguration().maxBatchSize();
        if (size == 0 || size > maxBatchSize) {
            throw new IllegalArgumentException(
                    "Batch size must be between 1 and " + maxBatchSize);
        }
    }

    private String normalize(String phone) {
        return phone.replaceAll("[^0-9]", "");
    }

    private void sleep() {
        long delay = configurationService.getActiveConfiguration().bulkDelayMs();
        if (delay <= 0) return;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bulk sending interrupted", e);
        }
    }
}
