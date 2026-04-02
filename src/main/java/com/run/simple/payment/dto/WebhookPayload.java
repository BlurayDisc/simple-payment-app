package com.run.simple.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

import com.run.simple.payment.model.WebhookEventType;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * The JSON body delivered to every registered webhook URL after a payment is created. Raw card
 * number is never included — only cardLastFour from PaymentResponse.
 */
@Getter
@Builder
@Jacksonized
public class WebhookPayload {

  private final WebhookEventType eventType;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
      timezone = "UTC")
  private final Instant occurredAt;

  private final PaymentResponse payment;
}
