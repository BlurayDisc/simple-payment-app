package com.run.simple.payment.controller;

import com.run.simple.payment.dto.CreateWebhookRequest;
import com.run.simple.payment.dto.WebhookResponse;
import com.run.simple.payment.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Register and manage webhook endpoints")
public class WebhookController {

  private final WebhookService webhookService;

  @Operation(
      summary = "Register a webhook",
      description =
          "Registers an endpoint URL to receive a POST request after every new payment. URLs must be unique.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Webhook registered successfully",
        content = @Content(schema = @Schema(implementation = WebhookResponse.class))),
    @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content),
    @ApiResponse(responseCode = "409", description = "URL already registered", content = @Content)
  })
  @PostMapping
  public ResponseEntity<WebhookResponse> registerWebhook(
      @Valid @RequestBody CreateWebhookRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(webhookService.registerWebhook(request));
  }

  @Operation(summary = "List all registered webhooks")
  @ApiResponse(
      responseCode = "200",
      description = "List of all webhooks",
      content = @Content(schema = @Schema(implementation = WebhookResponse.class)))
  @GetMapping
  public ResponseEntity<List<WebhookResponse>> getAllWebhooks() {
    return ResponseEntity.ok(webhookService.getAllWebhooks());
  }

  @Operation(summary = "Delete a webhook by ID")
  @ApiResponses({
    @ApiResponse(
        responseCode = "204",
        description = "Webhook deleted successfully",
        content = @Content),
    @ApiResponse(responseCode = "404", description = "Webhook not found", content = @Content)
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteWebhook(@PathVariable UUID id) {
    webhookService.deleteWebhook(id);
    return ResponseEntity.noContent().build();
  }
}
