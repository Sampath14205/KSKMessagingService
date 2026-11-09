package com.example.whatsapp.service;

import com.example.whatsapp.model.WhatsAppConfiguration;
import com.example.whatsapp.repository.WhatsAppConfigurationRepository;
import org.springframework.stereotype.Service;

@Service
public class DatabaseConfigurationService {
    private final WhatsAppConfigurationRepository repository;

    public DatabaseConfigurationService(WhatsAppConfigurationRepository repository) {
        this.repository = repository;
    }

    public WhatsAppConfiguration getActiveConfiguration() {
        return repository.findFirstByActiveTrue()
                .orElseThrow(() -> new IllegalStateException(
                        "No active WhatsApp configuration found. Create one using /api/admin/whatsapp-configurations."));
    }

    public WhatsAppConfiguration getActiveConfiguration(String businessId) {
        if (businessId == null || businessId.isBlank()) return getActiveConfiguration();
        return repository.findByBusinessIdAndActiveTrue(businessId)
                .orElseThrow(() -> new IllegalStateException("No active WhatsApp configuration for businessId: " + businessId));
    }

    public WhatsAppConfiguration findByVerifyToken(String token) {
        return repository.findByVerifyToken(token).orElse(null);
    }

    public WhatsAppConfiguration findByPhoneNumberId(String phoneNumberId) {
        return repository.findByPhoneNumberIdAndActiveTrue(phoneNumberId).orElse(null);
    }
}
