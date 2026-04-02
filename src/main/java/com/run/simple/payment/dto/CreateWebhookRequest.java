package com.run.simple.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class CreateWebhookRequest {

  @NotBlank(message = "URL is required")
  @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
  @Size(max = 2048, message = "URL must not exceed 2048 characters")
  private final String url;

  @Size(max = 255, message = "Description must not exceed 255 characters")
  private final String description;
}
