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
@Schema(description = "Registered webhook details")
public class WebhookResponse {

  @Schema(
      description = "Unique webhook identifier",
      example = "e5f6g7h8-1234-5678-abcd-ef1234567890")
  UUID id;

  @Schema(
      description = "Endpoint URL that receives payment event POSTs",
      example = "https://your-service.com/payment-hook")
  String url;

  @Schema(description = "Optional description", example = "Production payment listener")
  String description;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
      timezone = "UTC")
  @Schema(
      description = "UTC timestamp when the webhook was registered",
      example = "2025-01-01T12:00:00Z")
  Instant createdAt;
}
