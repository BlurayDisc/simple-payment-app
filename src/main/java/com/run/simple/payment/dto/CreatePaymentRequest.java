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
@Schema(description = "Request payload to create a new payment")
public class CreatePaymentRequest {

  @NotBlank(message = "First name is required")
  @Size(max = 100, message = "First name must not exceed 100 characters")
  @Schema(description = "Cardholder first name", example = "John")
  String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 100, message = "Last name must not exceed 100 characters")
  @Schema(description = "Cardholder last name", example = "Doe")
  String lastName;

  @NotBlank(message = "Zip code is required")
  @Pattern(regexp = "^[0-9]{5}(-[0-9]{4})?$", message = "Zip code must be 5 digits or ZIP+4 format")
  @Schema(description = "Billing zip code", example = "10001")
  String zipCode;

  @NotBlank(message = "Card number is required")
  @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be 13-19 digits with no spaces")
  @Schema(
      description = "Full card number — encrypted at rest, never stored in plaintext",
      example = "4111111111111111")
  String cardNumber;
}
