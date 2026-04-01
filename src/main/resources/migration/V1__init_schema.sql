-- V1__init_schema.sql
-- Initial schema for simple-payment-app
-- Uses UUIDs as PKs, no ORM sequences needed.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── Payments ──────────────────────────────────────────────────────────────────
CREATE TABLE payment (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    zip_code        VARCHAR(20)  NOT NULL,
    -- AES-256-GCM encrypted card number, stored as Base64 text
    card_number_enc TEXT         NOT NULL,
    -- Last 4 digits stored in plaintext for display purposes only
    card_last_four  CHAR(4)      NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Webhooks ──────────────────────────────────────────────────────────────────
CREATE TABLE webhook (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    url         TEXT        NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Prevent duplicate webhook URLs
CREATE UNIQUE INDEX uidx_webhook_url ON webhook (url);

-- ── Webhook Delivery Log ──────────────────────────────────────────────────────
-- Every dispatch attempt is recorded here for observability and debugging.
CREATE TABLE webhook_delivery_log (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    webhook_id      UUID        NOT NULL REFERENCES webhook(id) ON DELETE CASCADE,
    payment_id      UUID        NOT NULL REFERENCES payment(id) ON DELETE CASCADE,
    attempt_number  SMALLINT    NOT NULL DEFAULT 1,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING | SUCCESS | FAILED
    http_status     INT,
    error_message   TEXT,
    attempted_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_delivery_log_webhook  ON webhook_delivery_log (webhook_id);
CREATE INDEX idx_delivery_log_payment  ON webhook_delivery_log (payment_id);
CREATE INDEX idx_delivery_log_status   ON webhook_delivery_log (status);
