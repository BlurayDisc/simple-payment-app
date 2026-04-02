package com.run.simple.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class WebhookResponse {

  private final UUID id;

  private final String url;

  String description;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
      timezone = "UTC")
  private final Instant createdAt;
}
