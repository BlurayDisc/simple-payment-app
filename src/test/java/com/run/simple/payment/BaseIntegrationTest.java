package com.run.simple.payment;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for all integration tests.
 *
 * Spins up a single PostgreSQL container shared across the entire test suite
 * via the static @Container field — Testcontainers reuses the same container
 * for all subclasses, keeping the suite fast.
 *
 * Flyway runs automatically on context startup, so the schema is always clean
 * and correct per the migration scripts.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("paymentdb_test")
          .withUsername("test_user")
          .withPassword("test_pass");

  @DynamicPropertySource
  static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @Autowired protected MockMvc mockMvc;

  @Autowired protected com.run.simple.payment.repository.PaymentRepository paymentRepository;

  @Autowired protected com.run.simple.payment.repository.WebhookRepository webhookRepository;

  @Autowired
  protected com.run.simple.payment.repository.WebhookDeliveryLogRepository deliveryLogRepository;

  /** Wipe all data between tests so each test starts from a clean state. */
  @BeforeEach
  void cleanDatabase() {
    deliveryLogRepository.deleteAll();
    paymentRepository.deleteAll();
    webhookRepository.deleteAll();
  }
}
