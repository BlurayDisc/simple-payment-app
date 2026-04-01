package com.run.simple.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

import java.time.Instant;
import java.util.Optional;

/**
 * Activates Spring Data JDBC auditing so that {@code @CreatedDate} fields
 * on {@code Payment}, {@code Webhook}, and {@code WebhookDeliveryLog} are
 * automatically populated with the current UTC instant on insert.
 *
 * <p>Without this config, {@code @CreatedDate} is silently ignored and the
 * field stays {@code null}.
 */
@Configuration
@EnableJdbcAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class AuditingConfig {

  /**
   * Provides {@code Instant} as the timestamp type for {@code @CreatedDate}.
   * Spring Data defaults to {@code LocalDateTime} — this override aligns with
   * the {@code TIMESTAMPTZ} columns in the DB schema.
   */
  @Bean
  public DateTimeProvider auditingDateTimeProvider() {
    return () -> Optional.of(Instant.now());
  }
}
