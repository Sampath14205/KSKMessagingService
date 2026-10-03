# Meta WhatsApp Cloud API - Spring Boot

Direct integration with Meta's WhatsApp Cloud API. No WATI, WhatChimp, Twilio, or other intermediary is used.

## Features

- Send a WhatsApp text message
- Send an approved WhatsApp template
- Bulk text sending with configurable delay
- Bulk template sending
- Meta webhook verification
- Receive inbound WhatsApp events/status callbacks
- X-Hub-Signature-256 validation
- Environment-variable based credentials
- Java 21 + Spring Boot 3

## 1. Meta prerequisites

Create/configure a Meta developer app with the WhatsApp product and obtain:

- WhatsApp Phone Number ID
- Access Token
- App Secret
- Webhook Verify Token (you create this yourself)
- A public HTTPS webhook URL

For production, use a proper system-user/permanent access-token setup rather than keeping temporary dashboard tokens.

## 2. Configure environment variables

Linux/macOS:

```bash
export META_PHONE_NUMBER_ID="YOUR_PHONE_NUMBER_ID"
export META_ACCESS_TOKEN="YOUR_ACCESS_TOKEN"
export META_VERIFY_TOKEN="YOUR_RANDOM_VERIFY_TOKEN"
export META_APP_SECRET="YOUR_META_APP_SECRET"
export META_GRAPH_VERSION="v23.0"
export META_BULK_DELAY_MS="100"
export META_MAX_BATCH_SIZE="1000"
```

Windows PowerShell:

```powershell
$env:META_PHONE_NUMBER_ID="YOUR_PHONE_NUMBER_ID"
$env:META_ACCESS_TOKEN="YOUR_ACCESS_TOKEN"
$env:META_VERIFY_TOKEN="YOUR_RANDOM_VERIFY_TOKEN"
$env:META_APP_SECRET="YOUR_META_APP_SECRET"
```

The Graph version is configurable. Use the currently supported version in your Meta dashboard/docs rather than hard-coding a version in application code.

## 3. Build and run

```bash
mvn clean package
java -jar target/meta-whatsapp-cloud-api-1.0.0.jar
```

Or:

```bash
mvn spring-boot:run
```

Health:

```text
GET http://localhost:8080/actuator/health
```

## 4. Send one text

```bash
curl -X POST http://localhost:8080/api/whatsapp/send/text \
  -H "Content-Type: application/json" \
  -d '{
    "to": "919876543210",
    "body": "Hello from my Spring Boot application",
    "previewUrl": false
  }'
```

## 5. Send a template

The template must already exist and be approved in WhatsApp Manager.

```bash
curl -X POST http://localhost:8080/api/whatsapp/send/template \
  -H "Content-Type: application/json" \
  -d '{
    "to": "919876543210",
    "templateName": "hello_world",
    "languageCode": "en_US",
    "bodyParameters": []
  }'
```

Example with variables:

```json
{
  "to": "919876543210",
  "templateName": "order_update",
  "languageCode": "en_US",
  "bodyParameters": ["Sampath", "12345"]
}
```

## 6. Bulk text

```bash
curl -X POST http://localhost:8080/api/whatsapp/send/bulk/text \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {"to":"919876543210","body":"Hello 1","previewUrl":false},
      {"to":"919876543211","body":"Hello 2","previewUrl":false}
    ]
  }'
```

## 7. Bulk templates

```bash
curl -X POST http://localhost:8080/api/whatsapp/send/bulk/template \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {
        "to":"919876543210",
        "templateName":"hello_world",
        "languageCode":"en_US",
        "bodyParameters":[]
      },
      {
        "to":"919876543211",
        "templateName":"hello_world",
        "languageCode":"en_US",
        "bodyParameters":[]
      }
    ]
  }'
```

## 8. Webhook

Configure the Meta webhook callback URL:

```text
https://YOUR_PUBLIC_DOMAIN/webhook/whatsapp
```

Verify token must equal:

```text
META_VERIFY_TOKEN
```

The POST endpoint validates `X-Hub-Signature-256` when `META_APP_SECRET` is configured.

Inbound events are currently logged to the application console. Extend `WhatsAppWebhookController` to deserialize the event and persist it to PostgreSQL/MongoDB.

## Important production notes

1. WhatsApp bulk messaging is not equivalent to unrestricted SMS blasting. Use only recipients you are permitted to message and follow Meta's WhatsApp Business Platform policies.
2. Outside the customer-service window, business-initiated conversations generally require an approved message template.
3. Do not commit access tokens or app secrets to Git.
4. Add a database-backed campaign/job table for large campaigns instead of holding the whole campaign in memory.
5. For serious production volume, replace the simple loop with a queue such as Kafka/RabbitMQ/SQS and implement retry/backoff/idempotency.
6. Add delivery-status persistence from webhook events.
7. Add authentication/authorization to your own `/api/whatsapp/**` endpoints before exposing them publicly.
8. Validate phone numbers and maintain consent/opt-out records.

## Suggested production architecture

Client/UI
  |
API Gateway/Auth
  |
Campaign Service
  |
Queue (Kafka/RabbitMQ)
  |
WhatsApp Sender Workers
  |
Meta WhatsApp Cloud API
  |
Meta Webhook
  |
Webhook Receiver
  |
DB (campaigns, recipients, messages, statuses)

This starter project intentionally keeps the first version small and directly connected to Meta.
