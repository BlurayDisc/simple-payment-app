package com.run.simple.payment.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.run.simple.payment.BaseIntegrationTest;
import com.run.simple.payment.model.DeliveryStatus;
import com.run.simple.payment.model.Webhook;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.TimeUnit;

/**
 * Integration tests for WebhookDispatcher resilience behaviour.
 *
 * Uses a real embedded HTTP server (com.sun.net.httpserver) to act as the
 * webhook target — this lets us control response codes and verify actual
 * outbound HTTP calls without mocking.
 *
 * Awaitility is used to assert on async outcomes without brittle Thread.sleep calls.
 */
@DisplayName("Webhook Dispatcher")
class WebhookDispatcherIT extends BaseIntegrationTest {

  @Autowired ObjectMapper objectMapper;

  private HttpServer targetServer;
  private String targetUrl;

  /** Hit counter so tests can assert how many times the dispatcher called the endpoint. */
  private final AtomicInteger hitCount = new AtomicInteger(0);

  @BeforeEach
  void startTargetServer() throws Exception {
    hitCount.set(0);
    targetServer = HttpServer.create(new InetSocketAddress(0), 0);
    int port = targetServer.getAddress().getPort();
    targetUrl = "http://localhost:" + port + "/webhook";
    targetServer.start();
  }

  @AfterEach
  void stopTargetServer() {
    if (targetServer != null) targetServer.stop(0);
  }

  // ── Happy path ────────────────────────────────────────────────────────────

  @Test
  @DisplayName("happy path — delivers webhook payload to registered URL after payment")
  void dispatch_happyPath_deliversSuccessfully() throws Exception {
    // arrange: endpoint always returns 200
    targetServer.createContext(
        "/webhook",
        exchange -> {
          hitCount.incrementAndGet();
          exchange.sendResponseHeaders(200, -1);
          exchange.close();
        });

    webhookRepository.save(Webhook.builder().id(UUID.randomUUID()).url(targetUrl).build());

    // act: create a payment — dispatch is async
    mockMvc
        .perform(
            post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "firstName",
                            "John",
                            "lastName",
                            "Doe",
                            "zipCode",
                            "10001",
                            "cardNumber",
                            "4111111111111111"))))
        .andExpect(status().isCreated());

    // assert: wait for async delivery to complete
    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              assertThat(hitCount.get()).isEqualTo(1);

              List<WebhookDeliveryLog> logs =
                  deliveryLogRepository.findByStatus(DeliveryStatus.SUCCESS);
              assertThat(logs).hasSize(1);
              assertThat(logs.get(0).getHttpStatus()).isEqualTo(200);
              assertThat(logs.get(0).getAttemptNumber()).isEqualTo(1);
            });
  }

  @Test
  @DisplayName("happy path — payment response is immediate even if webhook is slow")
  void dispatch_async_doesNotBlockPaymentResponse() throws Exception {
    // endpoint takes 3 seconds to respond — payment must still return instantly
    targetServer.createContext(
        "/webhook",
        exchange -> {
          hitCount.incrementAndGet();
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            exchange.sendResponseHeaders(200, -1);
          exchange.close();
        });

    webhookRepository.save(Webhook.builder().id(UUID.randomUUID()).url(targetUrl).build());

    long start = System.currentTimeMillis();

    mockMvc
        .perform(
            post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "firstName",
                            "Jane",
                            "lastName",
                            "Doe",
                            "zipCode",
                            "10001",
                            "cardNumber",
                            "4111111111111111"))))
        .andExpect(status().isCreated());

    long elapsed = System.currentTimeMillis() - start;

    // payment response must complete well under the 3s webhook delay
    assertThat(elapsed).isLessThan(2_000);
  }

  // ── Failure & retry ───────────────────────────────────────────────────────

  @Test
  @DisplayName("failure — retries on non-2xx and logs each failed attempt")
  void dispatch_nonSuccessResponse_retriesAndLogsFailures() throws Exception {
    // endpoint always returns 500
    targetServer.createContext(
        "/webhook",
        exchange -> {
          hitCount.incrementAndGet();
          exchange.sendResponseHeaders(500, -1);
          exchange.close();
        });

    webhookRepository.save(Webhook.builder().id(UUID.randomUUID()).url(targetUrl).build());

    mockMvc
        .perform(
            post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "firstName",
                            "John",
                            "lastName",
                            "Doe",
                            "zipCode",
                            "10001",
                            "cardNumber",
                            "4111111111111111"))))
        .andExpect(status().isCreated());

    // with test profile: max-attempts=2, initial-delay=100ms
    // wait long enough for all retries to exhaust
    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              assertThat(hitCount.get()).isEqualTo(2); // max-attempts from test profile

              List<WebhookDeliveryLog> logs =
                  deliveryLogRepository.findByStatus(DeliveryStatus.FAILED);
              assertThat(logs).hasSize(2);
              logs.forEach(
                  log -> {
                    assertThat(log.getHttpStatus()).isEqualTo(500);
                    assertThat(log.getErrorMessage()).contains("HTTP 500");
                  });
            });
  }

  @Test
  @DisplayName("failure — one webhook failure does not prevent delivery to others")
  void dispatch_oneWebhookFails_othersStillReceive() throws Exception {
    // first endpoint always fails
    HttpServer failingServer = HttpServer.create(new InetSocketAddress(0), 0);
    failingServer.createContext(
        "/webhook",
        exchange -> {
          exchange.sendResponseHeaders(500, -1);
          exchange.close();
        });
    failingServer.start();
    String failingUrl = "http://localhost:" + failingServer.getAddress().getPort() + "/webhook";

    // second endpoint succeeds
    AtomicInteger successHits = new AtomicInteger(0);
    targetServer.createContext(
        "/webhook",
        exchange -> {
          successHits.incrementAndGet();
          exchange.sendResponseHeaders(200, -1);
          exchange.close();
        });

    webhookRepository.save(Webhook.builder().id(UUID.randomUUID()).url(failingUrl).build());
    webhookRepository.save(Webhook.builder().id(UUID.randomUUID()).url(targetUrl).build());

    mockMvc
        .perform(
            post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "firstName",
                            "John",
                            "lastName",
                            "Doe",
                            "zipCode",
                            "10001",
                            "cardNumber",
                            "4111111111111111"))))
        .andExpect(status().isCreated());

    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              // successful webhook was still hit despite the other failing
              assertThat(successHits.get()).isEqualTo(1);

              List<WebhookDeliveryLog> successLogs =
                  deliveryLogRepository.findByStatus(DeliveryStatus.SUCCESS);
              assertThat(successLogs).hasSize(1);
            });

    failingServer.stop(0);
  }

  @Test
  @DisplayName("failure — connection refused is handled gracefully and logged")
  void dispatch_connectionRefused_logsFailureGracefully() throws Exception {
    // register a URL that nothing is listening on
    String deadUrl = "http://localhost:19999/dead-hook";

    webhookRepository.save(Webhook.builder().id(UUID.randomUUID()).url(deadUrl).build());

    mockMvc
        .perform(
            post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "firstName",
                            "John",
                            "lastName",
                            "Doe",
                            "zipCode",
                            "10001",
                            "cardNumber",
                            "4111111111111111"))))
        .andExpect(status().isCreated());

    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              List<WebhookDeliveryLog> failedLogs =
                  deliveryLogRepository.findByStatus(DeliveryStatus.FAILED);
              // all attempts exhausted and logged
              assertThat(failedLogs).isNotEmpty();
              failedLogs.forEach(
                  log -> {
                    assertThat(log.getErrorMessage()).isNotBlank();
                    assertThat(log.getHttpStatus())
                        .isNull(); // no HTTP response on connection failure
                  });
            });
  }
}
