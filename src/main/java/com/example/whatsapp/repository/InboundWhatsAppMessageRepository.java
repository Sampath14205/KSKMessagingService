package com.example.whatsapp.repository;

import com.example.whatsapp.model.InboundWhatsAppMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface InboundWhatsAppMessageRepository extends MongoRepository<InboundWhatsAppMessage, String> {
    List<InboundWhatsAppMessage> findAllByOrderByReceivedAtDesc();
}
