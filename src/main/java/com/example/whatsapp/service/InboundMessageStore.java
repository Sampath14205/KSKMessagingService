package com.example.whatsapp.service;

import com.example.whatsapp.model.InboundWhatsAppMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class InboundMessageStore {
    // Starter implementation: messages are kept in memory and disappear on restart.
    // Replace with a database for production.
    private static final int MAX_MESSAGES = 5000;
    private final ConcurrentLinkedDeque<InboundWhatsAppMessage> messages = new ConcurrentLinkedDeque<>();

    public void add(InboundWhatsAppMessage message) {
        messages.addFirst(message);
        while (messages.size() > MAX_MESSAGES) {
            messages.pollLast();
        }
    }

    public List<InboundWhatsAppMessage> findAll() {
        return new ArrayList<>(messages);
    }

    public List<InboundWhatsAppMessage> findByPhone(String phone) {
        String normalized = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        return messages.stream()
                .filter(m -> m.from() != null && m.from().replaceAll("[^0-9]", "").equals(normalized))
                .toList();
    }

    public int count() {
        return messages.size();
    }
}
