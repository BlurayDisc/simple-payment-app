package com.run.simple.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "webhook")
public class WebhookProperties {

  private Retry retry = new Retry();
  private Http http = new Http();

  @Data
  public static class Retry {
    private int maxAttempts = 3;
    private long initialDelayMs = 10_000L;
    private int multiplier = 3;
  }

  @Data
  public static class Http {
    private int connectTimeoutMs = 5_000;
    private int readTimeoutMs = 10_000;
  }
}
