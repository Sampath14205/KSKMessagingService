package com.example.whatsapp.controller;

import com.example.whatsapp.config.MetaProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@RestController
@RequestMapping("/webhook/whatsapp")
public class WhatsAppWebhookController {

    private final MetaProperties props;

    public WhatsAppWebhookController(MetaProperties props) {
        this.props = props;
    }

    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String verifyToken,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {

        if ("subscribe".equals(mode) && props.verifyToken().equals(verifyToken)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).body("Verification failed");
    }

    @PostMapping
    public ResponseEntity<String> receive(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String payload) {

        if (!isValidSignature(signature, payload)) {
            return ResponseEntity.status(401).body("Invalid signature");
        }

        // TODO: Deserialize/store/process inbound messages and delivery statuses.
        // The raw JSON is intentionally accepted so Meta webhook schema changes
        // do not break the endpoint.
        System.out.println("META WEBHOOK: " + payload);
        return ResponseEntity.ok("EVENT_RECEIVED");
    }

    private boolean isValidSignature(String signature, String payload) {
        if (props.appSecret() == null || props.appSecret().isBlank()) {
            return true; // Configure META_APP_SECRET in production.
        }
        if (signature == null || !signature.startsWith("sha256=")) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    props.appSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = "sha256=" + HexFormat.of().formatHex(digest);
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
}
