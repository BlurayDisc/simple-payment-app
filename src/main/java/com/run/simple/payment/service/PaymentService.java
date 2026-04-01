package com.run.simple.payment.service;

import com.run.simple.payment.dto.CreatePaymentRequest;
import com.run.simple.payment.dto.PaymentResponse;
import com.run.simple.payment.encryption.AesGcmEncryptionService;
import com.run.simple.payment.exception.PaymentNotFoundException;
import com.run.simple.payment.mapper.PaymentMapper;
import com.run.simple.payment.model.Payment;
import com.run.simple.payment.repository.PaymentRepository;
import com.run.simple.payment.webhook.WebhookDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final AesGcmEncryptionService encryptionService;
  private final PaymentMapper paymentMapper;
  private final WebhookDispatcher webhookDispatcher;

  /**
   * Creates a payment:
   *
   * <ol>
   *   <li>Encrypts the card number with AES-256-GCM
   *   <li>Persists the payment row
   *   <li>Fires async webhook dispatch (non-blocking)
   * </ol>
   */
  public PaymentResponse createPayment(CreatePaymentRequest request) {
    String cardNumber = request.getCardNumber();
    String encryptedCard = encryptionService.encrypt(cardNumber);
    String lastFour = cardNumber.substring(cardNumber.length() - 4);

    Payment payment =
        Payment.builder()
            .id(UUID.randomUUID())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .zipCode(request.getZipCode())
            .cardNumberEnc(encryptedCard)
            .cardLastFour(lastFour)
            .build();

    Payment saved = paymentRepository.save(payment);
    log.info("Payment created: id={} cardLastFour={}", saved.getId(), saved.getCardLastFour());

    // Fire-and-forget: dispatch webhooks asynchronously on a virtual thread.
    // The HTTP response is returned to the caller immediately — webhook
    // delivery outcome does not affect the payment creation response.
    webhookDispatcher.dispatchAsync(saved);

    return paymentMapper.toResponse(saved);
  }

  public PaymentResponse getPayment(UUID id) {
    return paymentRepository
        .findById(id)
        .map(paymentMapper::toResponse)
        .orElseThrow(() -> new PaymentNotFoundException(id));
  }

  public List<PaymentResponse> getAllPayments() {
    return StreamSupport.stream(paymentRepository.findAll().spliterator(), false)
        .map(paymentMapper::toResponse)
        .toList();
  }
}
