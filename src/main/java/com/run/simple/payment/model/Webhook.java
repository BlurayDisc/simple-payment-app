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
 * Represents a registered webhook endpoint. After each payment is created, the application will
 * POST a payment event to every registered webhook URL.
 */
@Table("webhook")
@Value
@Builder
@With
public class Webhook {

  @Id UUID id;

  @Column("url")
  String url;

  @Column("description")
  String description;

  @CreatedDate
  @Column("created_at")
  Instant createdAt;
}
