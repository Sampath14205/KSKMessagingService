package com.example.whatsapp.controller;

import com.example.whatsapp.dto.BusinessInfoRequest;
import com.example.whatsapp.dto.WhatsAppConfigurationRequest;
import com.example.whatsapp.model.BusinessInfo;
import com.example.whatsapp.model.WhatsAppConfiguration;
import com.example.whatsapp.repository.BusinessInfoRepository;
import com.example.whatsapp.repository.WhatsAppConfigurationRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminConfigurationController {
    private final BusinessInfoRepository businesses;
    private final WhatsAppConfigurationRepository configurations;

    public AdminConfigurationController(BusinessInfoRepository businesses,
                                        WhatsAppConfigurationRepository configurations) {
        this.businesses = businesses;
        this.configurations = configurations;
    }

    @PostMapping("/businesses")
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessInfo saveBusiness(@Valid @RequestBody BusinessInfoRequest request) {
        Instant now = Instant.now();
        BusinessInfo existing = businesses.findByBusinessId(request.businessId()).orElse(null);
        BusinessInfo value = new BusinessInfo(existing == null ? null : existing.id(),
                request.businessId(), request.businessName(), request.legalName(), request.email(),
                request.phone(), request.website(), request.address(),
                existing == null ? now : existing.createdAt(), now);
        return businesses.save(value);
    }

    @GetMapping("/businesses")
    public List<BusinessInfo> businesses() {
        return businesses.findAll();
    }

    @GetMapping("/businesses/{businessId}")
    public BusinessInfo business(@PathVariable String businessId) {
        return businesses.findByBusinessId(businessId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found"));
    }

    @PostMapping("/whatsapp-configurations")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> saveConfiguration(@Valid @RequestBody WhatsAppConfigurationRequest request) {
        if (!businesses.findByBusinessId(request.businessId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Create the business in /api/admin/businesses before adding its WhatsApp configuration");
        }
        Instant now = Instant.now();
        WhatsAppConfiguration old = configurations.findByBusinessIdAndActiveTrue(request.businessId()).orElse(null);
        if (old == null) {
            old = configurations.findAll().stream()
                    .filter(c -> c.businessId().equals(request.businessId())).findFirst().orElse(null);
        }
        WhatsAppConfiguration saved = configurations.save(new WhatsAppConfiguration(
                old == null ? null : old.id(), request.businessId(),
                blankDefault(request.graphUrl(), "https://graph.facebook.com"),
                blankDefault(request.graphVersion(), "v23.0"),
                request.phoneNumberId(), request.accessToken(), request.verifyToken(), request.appSecret(),
                request.bulkDelayMs() == null ? 100 : request.bulkDelayMs(),
                request.maxBatchSize() == null ? 1000 : request.maxBatchSize(),
                request.active() == null || request.active(),
                old == null ? now : old.createdAt(), now));
        return safeView(saved);
    }

    @GetMapping("/whatsapp-configurations")
    public List<Map<String, Object>> configurations() {
        return configurations.findAll().stream().map(this::safeView).toList();
    }

    @GetMapping("/whatsapp-configurations/{businessId}")
    public Map<String, Object> configuration(@PathVariable String businessId) {
        WhatsAppConfiguration value = configurations.findAll().stream()
                .filter(c -> c.businessId().equals(businessId)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "WhatsApp configuration not found"));
        return safeView(value);
    }

    private Map<String, Object> safeView(WhatsAppConfiguration c) {
        return Map.of("businessId", c.businessId(), "graphUrl", c.graphUrl(),
                "graphVersion", c.graphVersion(), "phoneNumberId", c.phoneNumberId(),
                "accessTokenConfigured", c.accessToken() != null && !c.accessToken().isBlank(),
                "verifyTokenConfigured", c.verifyToken() != null && !c.verifyToken().isBlank(),
                "appSecretConfigured", c.appSecret() != null && !c.appSecret().isBlank(),
                "bulkDelayMs", c.bulkDelayMs(), "maxBatchSize", c.maxBatchSize(), "active", c.active());
    }

    private String blankDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
