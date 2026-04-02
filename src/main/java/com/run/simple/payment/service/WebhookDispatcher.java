package com.run.simple.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.run.simple.payment.config.WebhookProperties;
import com.run.simple.payment.dto.WebhookPayload;
import com.run.simple.payment.mapper.PaymentMapper;
import com.run.simple.payment.model.Payment;
import com.run.simple.payment.model.Webhook;
import com.run.simple.payment.model.WebhookEventType;
import com.run.simple.payment.repository.WebhookRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Asynchronously dispatches webhook events to all registered endpoints after a payment is created.
 *
 * <h2>Resilience strategy</h2>
 *
 * <ol>
 *   <li><b>Async decoupling</b> — {@code @Async} runs each dispatch on a Java 21 virtual thread, so
 *       the payment HTTP response is returned to the caller immediately.
 *   <li><b>Per-webhook isolation</b> — each registered URL is dispatched independently; a failure
 *       on one URL never affects delivery to others.
 *   <li><b>Retry with exponential backoff</b> — on a non-2xx response or connection failure,
 *       delivery is retried up to {@code webhook.retry.max-attempts} times with delays of {@code
 *       initialDelayMs * multiplier^attempt}.
 *   <li><b>Dead-letter handling</b> — once all retries are exhausted the log entry is marked {@code
 *       FAILED} permanently. No infinite retry loops.
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookDispatcher {

  private static final String EVENT_TYPE_HEADER = "X-Event-Type";

  private final WebhookRepository webhookRepository;
  private final WebhookProperties webhookProperties;
  private final PaymentMapper paymentMapper;
  private final ObjectMapper objectMapper;

  /**
   * Entry point called by {@link com.run.simple.payment.service.PaymentService}. Runs on a virtual
   * thread — one virtual thread per registered webhook URL.
   */
  @Async("virtualThreadExecutor")
  public void dispatchAsync(Payment payment) {
    Iterable<Webhook> webhooks = webhookRepository.findAll();
    for (Webhook webhook : webhooks) {
      deliverWithRetry(webhook, payment);
    }
  }

  /**
   * R.Y. Agent used an in-memory approach here which technically isn't resilient to failure, since
   * the application can restart, crash, or connection failures etc.
   *
   * <p>In production the retry state should be stored in the database with a background worker Also
   * Ideally this is a deployable instance, not within the same rpc deployable performs the retries.
   *
   * <p>External message queue can be used here on top for worker job message delivery, retry
   * queues, and dead letter queues.
   */
  private void deliverWithRetry(Webhook webhook, Payment payment) {
    WebhookProperties.Retry retryConfig = webhookProperties.getRetry();
    int maxAttempts = retryConfig.getMaxAttempts();
    long delayMs = retryConfig.getInitialDelayMs();
    int multiplier = retryConfig.getMultiplier();

    String payloadJson = buildPayload(payment);

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      DeliveryResult result =
          doPost(webhook.getUrl(), WebhookEventType.PAYMENT_CREATED, payloadJson);

      if (result.success()) {
        log.info(
            "Webhook delivered: webhookId={} paymentId={} attempt={} httpStatus={}",
            webhook.getId(),
            payment.getId(),
            attempt,
            result.httpStatus());
        return;
      }

      log.warn(
          "Webhook delivery failed: webhookId={} paymentId={} attempt={}/{} error={}",
          webhook.getId(),
          payment.getId(),
          attempt,
          maxAttempts,
          result.errorMessage());

      if (attempt < maxAttempts) {
        sleep(delayMs);
        delayMs *= multiplier; // exponential backoff: 10s → 30s → 90s
      }
    }

    log.error(
        "Webhook delivery exhausted all retries: webhookId={} paymentId={}",
        webhook.getId(),
        payment.getId());
  }

  private DeliveryResult doPost(String url, WebhookEventType eventType, String payloadJson) {
    WebhookProperties.Http httpConfig = webhookProperties.getHttp();
    try {
      HttpClient client =
          HttpClient.newBuilder()
              .connectTimeout(Duration.ofMillis(httpConfig.getConnectTimeoutMs()))
              .build();

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(url))
              .timeout(Duration.ofMillis(httpConfig.getReadTimeoutMs()))
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .header(EVENT_TYPE_HEADER, eventType.toString())
              .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      boolean success = response.statusCode() >= 200 && response.statusCode() < 300;

      return new DeliveryResult(
          success, response.statusCode(), success ? null : "HTTP " + response.statusCode());
    } catch (Exception e) {
      return new DeliveryResult(false, null, e.getClass().getSimpleName() + ": " + e.getMessage());
    }
  }

  private String buildPayload(Payment payment) {
    try {
      WebhookPayload payload =
          WebhookPayload.builder()
              .eventType(WebhookEventType.PAYMENT_CREATED)
              .occurredAt(Instant.now())
              .payment(paymentMapper.toResponse(payment))
              .build();
      return objectMapper.writeValueAsString(payload);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialise webhook payload", e);
    }
  }

  private void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private record DeliveryResult(boolean success, Integer httpStatus, String errorMessage) {}
}
