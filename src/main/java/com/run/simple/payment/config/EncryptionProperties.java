package com.run.simple.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "encryption")
public class EncryptionProperties {

  /** Base64-encoded 32-byte (256-bit) AES key. Generate with: openssl rand -base64 32 */
  private String secretKey;
}
