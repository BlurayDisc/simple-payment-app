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
 * Aggregate root for a payment transaction.
 *
 * <p>card_number_enc – AES-256-GCM encrypted, Base64-encoded card number. card_last_four –
 * Plaintext last 4 digits for display only; full PAN is never exposed.
 */
@Data
@Table("payment")
@Builder
public class Payment {

  @Id private UUID id;

  @Column("first_name")
  private String firstName;

  @Column("last_name")
  private String lastName;

  @Column("zip_code")
  private String zipCode;

  @Column("card_number_enc")
  private String cardNumberEnc;

  @Column("card_last_four")
  private String cardLastFour;

  @CreatedDate
  @Column("created_at")
  private Instant createdAt;
}
