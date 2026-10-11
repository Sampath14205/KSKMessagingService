package com.example.whatsapp.controller;

import com.example.whatsapp.model.InboundWhatsAppMessage;
import com.example.whatsapp.service.InboundMessageStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp/inbound")
public class InboundMessagesController {
    private final InboundMessageStore store;

    public InboundMessagesController(InboundMessageStore store) {
        this.store = store;
    }

    /** List recent inbound messages accepted by the webhook. */
    @GetMapping("/messages")
    public ResponseEntity<List<InboundWhatsAppMessage>> getMessages(
            @RequestParam(defaultValue = "100") int limit) {
        if (limit < 1 || limit > 1000) {
            return ResponseEntity.badRequest().build();
        }
        List<InboundWhatsAppMessage> result = store.findAll().stream().limit(limit).toList();
        return ResponseEntity.ok(result);
    }

    /** Filter recent inbound messages by sender phone number, digits with country code. */
    @GetMapping("/messages/by-phone")
    public ResponseEntity<List<InboundWhatsAppMessage>> getMessagesByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(store.findByPhone(phone));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> count() {
        return ResponseEntity.ok(Map.of("count", store.count()));
    }
}
