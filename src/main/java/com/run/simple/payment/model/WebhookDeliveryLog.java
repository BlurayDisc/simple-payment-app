package com.run.simple.payment.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit record for every webhook dispatch attempt. One row is written per attempt — so a single
 * (webhook, payment) pair can have up to {@code webhook.retry.max-attempts} rows, one per try.
 */
@Table("webhook_delivery_log")
@Value
@Builder
@With
public class WebhookDeliveryLog {

  @Id UUID id;

  @Column("webhook_id")
  UUID webhookId;

  @Column("payment_id")
  UUID paymentId;

  @Column("attempt_number")
  int attemptNumber;

  @Column("status")
  DeliveryStatus status;

  /** HTTP response code returned by the target endpoint, null on connection failure. */
  @Column("http_status")
  Integer httpStatus;

  /** Error message on failure (exception message or non-2xx body snippet). */
  @Column("error_message")
  String errorMessage;

  @CreatedDate
  @Column("attempted_at")
  Instant attemptedAt;
}
