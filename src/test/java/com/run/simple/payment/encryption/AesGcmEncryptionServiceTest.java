package com.run.simple.payment.encryption;

import static org.assertj.core.api.Assertions.*;

import com.run.simple.payment.config.EncryptionProperties;
import com.run.simple.payment.exception.EncryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AesGcmEncryptionService")
class AesGcmEncryptionServiceTest {

  // Valid 32-byte Base64 key for unit tests
  private static final String TEST_KEY = "dGVzdC1rZXktZm9yLXRlc3RzLW9ubHktMTIzNDU2Nzg=";

  private AesGcmEncryptionService encryptionService;

  @BeforeEach
  void setUp() {
    EncryptionProperties props = new EncryptionProperties();
    props.setSecretKey(TEST_KEY);
    encryptionService = new AesGcmEncryptionService(props);
  }

  @Test
  @DisplayName("encrypt then decrypt returns original plaintext")
  void encryptDecrypt_roundtrip() {
    String original = "4111111111111111";
    String encrypted = encryptionService.encrypt(original);
    String decrypted = encryptionService.decrypt(encrypted);

    assertThat(decrypted).isEqualTo(original);
  }

  @Test
  @DisplayName("encrypt produces different ciphertext each call (unique IV per call)")
  void encrypt_producesUniqueOutputEachCall() {
    String plaintext = "4111111111111111";
    String enc1 = encryptionService.encrypt(plaintext);
    String enc2 = encryptionService.encrypt(plaintext);

    // Same plaintext, different IVs → different ciphertext
    assertThat(enc1).isNotEqualTo(enc2);
    // But both decrypt to the same value
    assertThat(encryptionService.decrypt(enc1)).isEqualTo(plaintext);
    assertThat(encryptionService.decrypt(enc2)).isEqualTo(plaintext);
  }

  @Test
  @DisplayName("encrypted value is never equal to plaintext")
  void encrypt_neverStoresPlaintext() {
    String plaintext = "4111111111111111";
    String ciphertext = encryptionService.encrypt(plaintext);

    assertThat(ciphertext).isNotEqualTo(plaintext);
    assertThat(ciphertext).doesNotContain(plaintext);
  }

  @Test
  @DisplayName("tampered ciphertext throws EncryptionException on decrypt")
  void decrypt_tamperedCiphertext_throwsException() {
    String encrypted = encryptionService.encrypt("4111111111111111");
    // flip last character to simulate tampering — GCM auth tag will fail
    String tampered = encrypted.substring(0, encrypted.length() - 1) + "X";

    assertThatThrownBy(() -> encryptionService.decrypt(tampered))
        .isInstanceOf(EncryptionException.class);
  }

  @Test
  @DisplayName("invalid key length throws on startup")
  void constructor_invalidKeyLength_throwsIllegalArgument() {
    EncryptionProperties badProps = new EncryptionProperties();
    badProps.setSecretKey("dG9vc2hvcnQ="); // too short

    assertThatThrownBy(() -> new AesGcmEncryptionService(badProps))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("32 bytes");
  }
}
