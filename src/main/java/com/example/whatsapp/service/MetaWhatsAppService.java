package com.example.whatsapp.service;

import com.example.whatsapp.config.MetaProperties;
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
    private final MetaProperties props;

    public MetaWhatsAppService(RestClient restClient, MetaProperties props) {
        this.restClient = restClient;
        this.props = props;
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
        String url = props.graphUrl() + "/" + props.graphVersion()
                + "/" + props.phoneNumberId() + "/messages";

        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + props.accessToken())
                .body(payload)
                .retrieve()
                .body(Object.class);
    }

    private void validateBatch(int size) {
        if (size == 0 || size > props.maxBatchSize()) {
            throw new IllegalArgumentException(
                    "Batch size must be between 1 and " + props.maxBatchSize());
        }
    }

    private String normalize(String phone) {
        return phone.replaceAll("[^0-9]", "");
    }

    private void sleep() {
        if (props.bulkDelayMs() <= 0) return;
        try {
            Thread.sleep(props.bulkDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bulk sending interrupted", e);
        }
    }
}
