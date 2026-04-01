package com.run.simple.payment.controller;

import com.run.simple.payment.dto.CreatePaymentRequest;
import com.run.simple.payment.dto.PaymentResponse;
import com.run.simple.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Create and retrieve payments")
public class PaymentController {

  private final PaymentService paymentService;

  @Operation(
      summary = "Create a new payment",
      description =
          "Accepts cardholder details and card number. "
              + "The card number is encrypted at rest using AES-256-GCM. "
              + "All registered webhooks are notified asynchronously.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Payment created successfully",
        content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
    @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content),
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
  })
  @PostMapping
  public ResponseEntity<PaymentResponse> createPayment(
      @Valid @RequestBody CreatePaymentRequest request) {

    PaymentResponse response = paymentService.createPayment(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "Get payment by ID")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Payment found",
        content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
    @ApiResponse(responseCode = "404", description = "Payment not found", content = @Content)
  })
  @GetMapping("/{id}")
  public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id) {
    return ResponseEntity.ok(paymentService.getPayment(id));
  }

  @Operation(summary = "List all payments")
  @ApiResponse(
      responseCode = "200",
      description = "List of all payments",
      content = @Content(schema = @Schema(implementation = PaymentResponse.class)))
  @GetMapping
  public ResponseEntity<List<PaymentResponse>> getAllPayments() {
    return ResponseEntity.ok(paymentService.getAllPayments());
  }
}
