package com.run.simple.payment.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Represents a registered webhook endpoint. After each payment is created, the application will
 * POST a payment event to every registered webhook URL.
 */
@Data
@Table("webhook")
@Builder
public class Webhook {

  @Id private UUID id;

  @Column("url")
  private String url;

  @Column("description")
  private String description;

  @CreatedDate
  @Column("created_at")
  private Instant createdAt;
}
