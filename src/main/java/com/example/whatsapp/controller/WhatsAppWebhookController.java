package com.example.whatsapp.controller;

import com.example.whatsapp.model.WhatsAppConfiguration;
import com.example.whatsapp.service.DatabaseConfigurationService;
import com.example.whatsapp.model.InboundWhatsAppMessage;
import com.example.whatsapp.service.InboundMessageStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@RestController
@RequestMapping("/webhook/whatsapp")
public class WhatsAppWebhookController {
    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    private final DatabaseConfigurationService configurationService;
    private final ObjectMapper objectMapper;
    private final InboundMessageStore store;

    public WhatsAppWebhookController(DatabaseConfigurationService configurationService,
                                     ObjectMapper objectMapper, InboundMessageStore store) {
        this.configurationService = configurationService;
        this.objectMapper = objectMapper;
        this.store = store;
    }

    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String verifyToken,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {
        WhatsAppConfiguration config = verifyToken == null ? null
                : configurationService.findByVerifyToken(verifyToken);
        if ("subscribe".equals(mode) && config != null && config.active()) {
            return ResponseEntity.ok(challenge == null ? "" : challenge);
        }
        return ResponseEntity.status(403).body("Verification failed");
    }

    @PostMapping
    public ResponseEntity<String> receive(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String phoneNumberId = root.path("entry").path(0).path("changes").path(0)
                    .path("value").path("metadata").path("phone_number_id").asText("");
            WhatsAppConfiguration config = configurationService.findByPhoneNumberId(phoneNumberId);
            if (config == null || !isValidSignature(signature, payload, config.appSecret())) {
                log.warn("Rejected WhatsApp webhook: configuration missing or invalid signature");
                return ResponseEntity.status(401).body("Invalid signature or configuration");
            }
            JsonNode entries = root.path("entry");
            if (entries.isArray()) {
                for (JsonNode entry : entries) {
                    JsonNode changes = entry.path("changes");
                    if (!changes.isArray()) continue;
                    for (JsonNode change : changes) {
                        if (!"messages".equals(change.path("field").asText())) continue;
                        JsonNode value = change.path("value");
                        JsonNode metadata = value.path("metadata");
                        JsonNode contacts = value.path("contacts");
                        JsonNode messages = value.path("messages");

                        if (messages.isArray()) {
                            for (JsonNode message : messages) {
                                String from = message.path("from").asText("");
                                String profileName = findProfileName(contacts, from);
                                String type = message.path("type").asText("unknown");
                                String body = extractBody(message, type);
                                InboundWhatsAppMessage inbound = new InboundWhatsAppMessage(
                                        message.path("id").asText(""),
                                        from,
                                        profileName,
                                        type,
                                        body,
                                        message.path("timestamp").asText(""),
                                        metadata.path("phone_number_id").asText(""),
                                        metadata.path("display_phone_number").asText(""),
                                        Instant.now()
                                );
                                store.add(inbound);
                                log.info("WhatsApp inbound message received: from={}, type={}, messageId={}",
                                        from, type, inbound.messageId());
                            }
                        }

                        // Delivery/read/failure status events are acknowledged and logged.
                        JsonNode statuses = value.path("statuses");
                        if (statuses.isArray()) {
                            for (JsonNode status : statuses) {
                                log.info("WhatsApp message status: id={}, status={}, recipient={}",
                                        status.path("id").asText(""),
                                        status.path("status").asText(""),
                                        status.path("recipient_id").asText(""));
                            }
                        }
                    }
                }
            }
            // Respond promptly so Meta does not keep retrying an already accepted webhook.
            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (Exception ex) {
            log.error("Could not parse WhatsApp webhook payload", ex);
            // Malformed payloads should be visible in logs; 400 indicates it was not accepted.
            return ResponseEntity.badRequest().body("Invalid webhook payload");
        }
    }

    private String findProfileName(JsonNode contacts, String from) {
        if (!contacts.isArray()) return "";
        for (JsonNode contact : contacts) {
            if (from.equals(contact.path("wa_id").asText(""))) {
                return contact.path("profile").path("name").asText("");
            }
        }
        return "";
    }

    private String extractBody(JsonNode message, String type) {
        switch (type) {
            case "text":
                return message.path("text").path("body").asText("");
            case "button":
                return message.path("button").path("text").asText("");
            case "interactive":
                JsonNode interactive = message.path("interactive");
                JsonNode buttonReply = interactive.path("button_reply");
                JsonNode listReply = interactive.path("list_reply");
                if (!buttonReply.isMissingNode()) {
                    return buttonReply.path("title").asText("");
                }
                if (!listReply.isMissingNode()) {
                    return listReply.path("title").asText("");
                }
                return "";
            case "image":
                return "[image] " + message.path("image").path("id").asText("");
            case "document":
                return "[document] " + message.path("document").path("filename").asText("");
            case "audio":
                return "[audio] " + message.path("audio").path("id").asText("");
            case "video":
                return "[video] " + message.path("video").path("id").asText("");
            case "location":
                return "[location] " + message.path("location").path("latitude").asText("")
                        + "," + message.path("location").path("longitude").asText("");
            case "contacts":
                return "[contact card]";
            case "sticker":
                return "[sticker] " + message.path("sticker").path("id").asText("");
            default:
                return "";
        }
    }

    private boolean isValidSignature(String signature, String payload, String appSecret) {
        if (appSecret == null || appSecret.isBlank()) return false;
        if (signature == null || !signature.startsWith("sha256=")) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = "sha256=" + HexFormat.of().formatHex(digest);
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
}
