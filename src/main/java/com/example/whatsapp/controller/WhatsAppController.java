package com.example.whatsapp.controller;

import com.example.whatsapp.dto.*;
import com.example.whatsapp.service.MetaWhatsAppService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    private final MetaWhatsAppService service;

    public WhatsAppController(MetaWhatsAppService service) {
        this.service = service;
    }

    @PostMapping("/send/text")
    public Object sendText(@Valid @RequestBody SendTextRequest request) {
        return service.sendText(request);
    }

    @PostMapping("/send/template")
    public Object sendTemplate(@Valid @RequestBody TemplateRequest request) {
        return service.sendTemplate(request);
    }

    @PostMapping("/send/bulk/text")
    public ResponseEntity<?> bulkText(@Valid @RequestBody BulkTextRequest request) {
        return ResponseEntity.accepted().body(
                java.util.Map.of("status", "accepted",
                        "messageCount", request.messages().size(),
                        "job", service.bulkText(request.messages()))
        );
    }

    @PostMapping("/send/bulk/template")
    public ResponseEntity<?> bulkTemplate(@Valid @RequestBody BulkTemplateRequest request) {
        return ResponseEntity.accepted().body(
                java.util.Map.of("status", "accepted",
                        "messageCount", request.messages().size(),
                        "job", service.bulkTemplate(request.messages()))
        );
    }
}
