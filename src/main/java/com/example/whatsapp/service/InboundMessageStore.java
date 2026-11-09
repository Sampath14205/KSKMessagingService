package com.example.whatsapp.service;

import com.example.whatsapp.model.InboundWhatsAppMessage;
import com.example.whatsapp.repository.InboundWhatsAppMessageRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InboundMessageStore {
    private final InboundWhatsAppMessageRepository repository;

    public InboundMessageStore(InboundWhatsAppMessageRepository repository) {
        this.repository = repository;
    }

    public void add(InboundWhatsAppMessage message) {
        repository.save(message);
    }

    public List<InboundWhatsAppMessage> findAll() {
        return repository.findAllByOrderByReceivedAtDesc();
    }

    public List<InboundWhatsAppMessage> findByPhone(String phone) {
        String normalized = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        return findAll().stream()
                .filter(m -> m.from() != null && m.from().replaceAll("[^0-9]", "").equals(normalized))
                .toList();
    }

    public int count() {
        return (int) repository.count();
    }
}
