package com.run.simple.payment.controller;

import com.run.simple.payment.dto.CreateWebhookRequest;
import com.run.simple.payment.dto.WebhookResponse;
import com.run.simple.payment.service.WebhookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Register and manage webhook endpoints")
public class WebhookController {

  private final WebhookService webhookService;

  @PostMapping
  public ResponseEntity<WebhookResponse> registerWebhook(
      @Valid @RequestBody CreateWebhookRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(webhookService.registerWebhook(request));
  }

  @GetMapping
  public ResponseEntity<List<WebhookResponse>> getAllWebhooks() {
    return ResponseEntity.ok(webhookService.getAllWebhooks());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteWebhook(@PathVariable UUID id) {
    webhookService.deleteWebhook(id);
    return ResponseEntity.noContent().build();
  }
}
