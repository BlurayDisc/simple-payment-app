package com.run.simple.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class WebhookResponse {

  @Schema(
      description = "Unique webhook identifier",
      example = "e5f6g7h8-1234-5678-abcd-ef1234567890")
  private final UUID id;

  @Schema(
      description = "Endpoint URL that receives payment event POSTs",
      example = "https://your-service.com/payment-hook")
  private final String url;

  @Schema(description = "Optional description", example = "Production payment listener")
  String description;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
      timezone = "UTC")
  private final Instant createdAt;
}
