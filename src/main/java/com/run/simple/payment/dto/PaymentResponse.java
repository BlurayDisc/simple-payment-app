package com.run.simple.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
@Jacksonized
@Schema(description = "Payment details returned after creation or retrieval")
public class PaymentResponse {

  @Schema(
      description = "Unique payment identifier",
      example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
  UUID id;

  @Schema(description = "Cardholder first name", example = "John")
  String firstName;

  @Schema(description = "Cardholder last name", example = "Doe")
  String lastName;

  @Schema(description = "Billing zip code", example = "10001")
  String zipCode;

  @Schema(description = "Last 4 digits of the card — full PAN is never exposed", example = "1111")
  String cardLastFour;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
      timezone = "UTC")
  @Schema(description = "UTC timestamp of payment creation", example = "2025-01-01T12:00:00Z")
  Instant createdAt;
}
