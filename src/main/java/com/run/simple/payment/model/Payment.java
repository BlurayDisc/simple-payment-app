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
 * Aggregate root for a payment transaction.
 *
 * <p>card_number_enc – AES-256-GCM encrypted, Base64-encoded card number. card_last_four –
 * Plaintext last 4 digits for display only; full PAN is never exposed.
 */
@Table("payment")
@Value
@Builder
@With
public class Payment {

  @Id UUID id;

  @Column("first_name")
  String firstName;

  @Column("last_name")
  String lastName;

  @Column("zip_code")
  String zipCode;

  @Column("card_number_enc")
  String cardNumberEnc;

  @Column("card_last_four")
  String cardLastFour;

  @CreatedDate
  @Column("created_at")
  Instant createdAt;
}
