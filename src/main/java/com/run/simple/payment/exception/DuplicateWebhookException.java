package com.run.simple.payment.exception;

public class DuplicateWebhookException extends RuntimeException {

  public DuplicateWebhookException(String url) {
    super("A webhook is already registered for URL: " + url);
  }
}
