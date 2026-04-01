package com.run.simple.payment.encryption;

import com.run.simple.payment.config.EncryptionProperties;
import com.run.simple.payment.exception.EncryptionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM symmetric encryption for card numbers.
 *
 * <p>Each encryption call generates a fresh 12-byte IV (nonce), which is prepended to the
 * ciphertext before Base64 encoding. Decryption reads the IV from the first 12 bytes of the decoded
 * payload.
 *
 * <p>GCM mode provides both confidentiality and integrity — any tampering with the ciphertext will
 * cause decryption to throw.
 */
@Slf4j
@Service
public class AesGcmEncryptionService {

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int GCM_TAG_BITS = 128; // authentication tag length
  private static final int IV_BYTES = 12; // 96-bit IV recommended for GCM

  private final SecretKey secretKey;
  private final SecureRandom secureRandom = new SecureRandom();

  public AesGcmEncryptionService(EncryptionProperties properties) {
    byte[] keyBytes = Base64.getDecoder().decode(properties.getSecretKey());
    if (keyBytes.length != 32) {
      throw new IllegalArgumentException(
          "Encryption key must be 32 bytes (256 bits). Got: "
              + keyBytes.length
              + " bytes. "
              + "Generate one with: openssl rand -base64 32");
    }
    this.secretKey = new SecretKeySpec(keyBytes, "AES");
  }

  /**
   * Encrypts {@code plaintext} and returns a Base64-encoded string of {@code [IV (12 bytes) ||
   * ciphertext+tag]}.
   */
  public String encrypt(String plaintext) {
    try {
      byte[] iv = new byte[IV_BYTES];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));

      byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

      // Prepend IV so decrypt can recover it
      byte[] ivAndCipher = new byte[IV_BYTES + ciphertext.length];
      System.arraycopy(iv, 0, ivAndCipher, 0, IV_BYTES);
      System.arraycopy(ciphertext, 0, ivAndCipher, IV_BYTES, ciphertext.length);

      return Base64.getEncoder().encodeToString(ivAndCipher);
    } catch (Exception e) {
      throw new EncryptionException("Failed to encrypt value", e);
    }
  }

  /**
   * Decrypts a Base64-encoded {@code [IV || ciphertext+tag]} payload produced by {@link
   * #encrypt(String)}.
   */
  public String decrypt(String encryptedBase64) {
    try {
      byte[] ivAndCipher = Base64.getDecoder().decode(encryptedBase64);

      byte[] iv = new byte[IV_BYTES];
      byte[] ciphertext = new byte[ivAndCipher.length - IV_BYTES];
      System.arraycopy(ivAndCipher, 0, iv, 0, IV_BYTES);
      System.arraycopy(ivAndCipher, IV_BYTES, ciphertext, 0, ciphertext.length);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));

      return new String(cipher.doFinal(ciphertext));
    } catch (Exception e) {
      throw new EncryptionException("Failed to decrypt value", e);
    }
  }
}
