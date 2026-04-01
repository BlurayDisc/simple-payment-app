package com.run.simple.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

/**
 * The JSON body delivered to every registered webhook URL after a payment is created.
 * Raw card number is never included — only cardLastFour from PaymentResponse.
 */
@Value
@Builder
@Jacksonized
@Schema(description = "Payload posted to webhook endpoints on payment events")
public class WebhookPayload {

  @Schema(description = "Event type identifier", example = "PAYMENT_CREATED")
  String eventType;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
      timezone = "UTC")
  @Schema(description = "UTC timestamp when the event occurred", example = "2025-01-01T12:00:00Z")
  Instant occurredAt;

  @Schema(description = "Payment details associated with this event")
  PaymentResponse payment;
}
