package com.run.simple.payment.controller.exception;

import com.run.simple.payment.exception.DuplicateWebhookException;
import com.run.simple.payment.exception.PaymentNotFoundException;
import com.run.simple.payment.exception.WebhookNotFoundException;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** 400 — bean validation failures on @Valid request bodies. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "invalid",
                    (a, b) -> a));
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Validation Failed");
    problem.setType(URI.create("urn:problem:validation-failed"));
    problem.setProperty("errors", fieldErrors);
    return problem;
  }

  /** 404 — payment not found. */
  @ExceptionHandler(PaymentNotFoundException.class)
  public ProblemDetail handlePaymentNotFound(PaymentNotFoundException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problem.setTitle("Payment Not Found");
    problem.setType(URI.create("urn:problem:payment-not-found"));
    problem.setDetail(ex.getMessage());
    return problem;
  }

  /** 404 — webhook not found. */
  @ExceptionHandler(WebhookNotFoundException.class)
  public ProblemDetail handleWebhookNotFound(WebhookNotFoundException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problem.setTitle("Webhook Not Found");
    problem.setType(URI.create("urn:problem:webhook-not-found"));
    problem.setDetail(ex.getMessage());
    return problem;
  }

  /** 409 — duplicate webhook URL. */
  @ExceptionHandler(DuplicateWebhookException.class)
  public ProblemDetail handleDuplicateWebhook(DuplicateWebhookException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problem.setTitle("Duplicate Webhook");
    problem.setType(URI.create("urn:problem:duplicate-webhook"));
    problem.setDetail(ex.getMessage());
    return problem;
  }

  /** 500 — catch-all for unexpected errors. */
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneric(Exception ex) {
    log.error("Unhandled exception", ex);
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problem.setTitle("Internal Server Error");
    problem.setType(URI.create("urn:problem:internal-error"));
    problem.setDetail("An unexpected error occurred. Please try again.");
    return problem;
  }
}
