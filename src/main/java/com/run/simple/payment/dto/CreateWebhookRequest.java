package com.run.simple.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@Schema(description = "Request payload to register a new webhook endpoint")
public class CreateWebhookRequest {

  @NotBlank(message = "URL is required")
  @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
  @Size(max = 2048, message = "URL must not exceed 2048 characters")
  @Schema(
      description = "The endpoint URL that will receive POST requests on payment events",
      example = "https://your-service.com/payment-hook")
  String url;

  @Size(max = 255, message = "Description must not exceed 255 characters")
  @Schema(
      description = "Optional human-readable description of this webhook",
      example = "Production payment listener")
  String description;
}
