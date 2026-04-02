# Simple Payment App

A Spring Boot REST API for processing payments with dynamic webhook notifications.

---

## Tech Stack

- **Java 21**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **PostgreSQL**
- **Flyway** (database migrations)
- **SpringDoc OpenAPI** (API documentation)
- **Docker & Docker Compose**

---

## Features

- Create payments with encrypted card number storage (AES-256-GCM)
- Register dynamic webhooks that are notified on every new payment
- Resilient webhook delivery with async processing, retry logic, and delivery audit logging
- OpenAPI specification at project root (`openapi.yaml`)

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven | 3.9+ |
| Docker | 24+ |
| Docker Compose | v2+ |

---

## Getting Started

### Option 1 - Run with Docker Compose (Recommended)

This starts both the PostgreSQL database and the application together.

```bash
# Clone the repository
git clone https://github.com/BlurayDisc/simple-payment-app.git
cd simple-payment-app

# Build the application JAR
./mvnw clean package -DskipTests

# Start all services
docker-compose up --build
```

The API will be available at: `http://localhost:8080`

To stop all services:
```bash
docker-compose down
```

To stop and remove all volumes (wipes the database):
```bash
docker-compose down -v
```

---

### Option 2 - Run with Maven (Local Development)

This requires a running PostgreSQL instance.

**Step 1 - Start PostgreSQL via Docker:**
```bash
docker run -d \
  --name payment-db \
  -e POSTGRES_DB=paymentdb \
  -e POSTGRES_USER=payment_user \
  -e POSTGRES_PASSWORD=payment_pass \
  -p 5432:5432 \
  postgres:16
```

**Step 2 - Run the application:**
```bash
./mvnw spring-boot:run
```

The API will be available at: `http://localhost:8080`

---

## API Documentation

Interactive Swagger UI is available at:
```
http://localhost:8080/swagger-ui.html
```

The full OpenAPI specification is available at the project: [`simple-payment.yaml`](./openapi/simple-payment.yaml)

---

## API Quick Reference

### Payments

#### Create a Payment
```bash
curl -X 'POST' \
  'http://localhost:8080/api/payments' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "firstName": "Run",
  "lastName": "Yan",
  "zipCode": "50093",
  "cardNumber": "9594500421057179"
}'
```

**Response `201 Created`:**
```json
{
  "id": "a1b2c3d4-...",
  "firstName": "John",
  "lastName": "Doe",
  "zipCode": "10001",
  "cardLastFour": "1111",
  "createdAt": "2025-01-01T12:00:00Z"
}
```

#### Get All Payments
```bash
curl http://localhost:8080/api/payments
```

#### Get Payment by ID
```bash
curl http://localhost:8080/api/payments/{id}
```

---

### Webhooks

#### Register a Webhook
```bash
curl -X 'POST' \
  'http://localhost:8080/api/webhooks' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "url": "https://webhook.site/80c728de-53eb-4fc6-b9c9-df874f3397b8",
  "description": "run'\''s webhook"
}'
```

**Response `201 Created`:**
```json
{
  "id": "e5f6g7h8-...",
  "url": "https://webhook.site/80c728de-53eb-4fc6-b9c9-df874f3397b8",
  "description": "run's webhook",
  "createdAt": "2025-01-01T12:00:00Z"
}
```

#### List All Webhooks
```bash
curl http://localhost:8080/api/webhooks
```

#### Delete a Webhook
```bash
curl -X DELETE http://localhost:8080/api/webhooks/{id}
```

---

### Webhook Payload

When a payment is created, all registered webhooks receive a POST request with:

```json
{
  "eventType": "PAYMENT_CREATED",
  "occurredAt": "2025-01-01T12:00:00Z",
  "payment": {
    "id": "a1b2c3d4-...",
    "firstName": "John",
    "lastName": "Doe",
    "zipCode": "10001",
    "cardLastFour": "1111",
    "createdAt": "2025-01-01T12:00:00Z"
  }
}
```

> Note: The raw card number is **never** included in any API response or webhook payload. Only the last 4 digits are exposed.

---

## Webhook Resilience

The webhook delivery system is designed to be resilient to endpoint failures:

- **Asynchronous** - webhook dispatch never blocks the payment creation response
- **Retry with exponential backoff** - failed deliveries are retried up to 3 times (delays: 10s → 30s → 90s)
- **Independent delivery** - a failure delivering to one webhook URL does not affect delivery to others

---

## Configuration

Key properties in `src/main/resources/application.properties`:

| Property | Description | Default |
|----------|-------------|---------|
| `encryption.secret-key` | AES-256 key for card encryption | (required) |
| `webhook.retry.max-attempts` | Max delivery retry attempts | `3` |
| `webhook.retry.initial-delay-ms` | Initial retry delay in ms | `10000` |
| `spring.datasource.url` | Database URL | set via env |

Environment variables (used by Docker Compose):

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=paymentdb
DB_USER=payment_user
DB_PASSWORD=payment_pass
ENCRYPTION_SECRET_KEY=your-32-char-secret-key-here
```

---

## Running Tests

```bash
# Run all tests
./mvnw test

# Run with integration tests (requires Docker for Testcontainers)
./mvnw verify
```

---

## Project Structure

```
simple-payment-app/
├── openapi.yaml                    # OpenAPI specification
├── docker-compose.yml
├── Dockerfile
├── README.md
└── src/
    └── main/
        ├── java/com/example/payment/
        │   ├── controller/         # REST controllers
        │   ├── service/            # Business logic, Webhook dispatcher & retry logic, AES-256-GCM encryption utilities
        │   ├── repository/         # JPA repositories
        │   ├── model/              # JPA entities
        │   ├── dto/                # Request/response DTOs
        └── resources/
            ├── application.properties
            └── db/migration/       # Flyway SQL migrations
```

---

## AI Assistance

This project was built with the assistance of Claude (Anthropic) as an agentic coding tool. The full conversation transcript is available at: *(link to be added)*

---

## License

MIT
