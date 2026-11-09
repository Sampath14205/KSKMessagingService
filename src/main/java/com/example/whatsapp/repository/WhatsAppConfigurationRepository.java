package com.example.whatsapp.repository;

import com.example.whatsapp.model.WhatsAppConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface WhatsAppConfigurationRepository extends MongoRepository<WhatsAppConfiguration, String> {
    Optional<WhatsAppConfiguration> findByBusinessIdAndActiveTrue(String businessId);
    Optional<WhatsAppConfiguration> findFirstByActiveTrue();
    Optional<WhatsAppConfiguration> findByVerifyToken(String verifyToken);
    Optional<WhatsAppConfiguration> findByPhoneNumberIdAndActiveTrue(String phoneNumberId);
}
