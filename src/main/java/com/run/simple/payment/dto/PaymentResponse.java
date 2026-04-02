package com.run.simple.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class PaymentResponse {

  private final UUID id;

  private final String firstName;

  private final String lastName;

  private final String zipCode;

  private final String cardLastFour;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
      timezone = "UTC")
  private final Instant createdAt;
}
