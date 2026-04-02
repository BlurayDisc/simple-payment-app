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
public class CreatePaymentRequest {

  @NotBlank(message = "First name is required")
  @Size(max = 100, message = "First name must not exceed 100 characters")
  private final String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 100, message = "Last name must not exceed 100 characters")
  private final String lastName;

  @NotBlank(message = "Zip code is required")
  @Pattern(regexp = "^[0-9]{5}(-[0-9]{4})?$", message = "Zip code must be 5 digits or ZIP+4 format")
  private final String zipCode;

  @NotBlank(message = "Card number is required")
  @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be 13-19 digits with no spaces")
  private final String cardNumber;
}
