package com.run.simple.payment.controller;

import com.run.simple.payment.dto.CreatePaymentRequest;
import com.run.simple.payment.dto.PaymentResponse;
import com.run.simple.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Create and retrieve payments")
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping
  public ResponseEntity<PaymentResponse> createPayment(
      @Valid @RequestBody CreatePaymentRequest request) {

    PaymentResponse response = paymentService.createPayment(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id) {
    return ResponseEntity.ok(paymentService.getPayment(id));
  }

  @GetMapping
  public ResponseEntity<List<PaymentResponse>> getAllPayments() {
    return ResponseEntity.ok(paymentService.getAllPayments());
  }
}
