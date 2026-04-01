package com.run.simple.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.run.simple.payment.BaseIntegrationTest;
import com.run.simple.payment.model.Webhook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Webhook API")
class WebhookControllerIT extends BaseIntegrationTest {

  @Autowired ObjectMapper objectMapper;

  // ── POST /api/webhooks ────────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/webhooks")
  class RegisterWebhook {

    @Test
    @DisplayName("happy path — registers webhook and returns 201")
    void registerWebhook_happyPath() throws Exception {
      var body =
          Map.of(
              "url", "https://example.com/hook",
              "description", "Test webhook");

      mockMvc
          .perform(
              post("/api/webhooks")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(body)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").isNotEmpty())
          .andExpect(jsonPath("$.url").value("https://example.com/hook"))
          .andExpect(jsonPath("$.description").value("Test webhook"))
          .andExpect(jsonPath("$.createdAt").isNotEmpty());

      assertThat(webhookRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("failure — duplicate URL returns 409")
    void registerWebhook_duplicateUrl_returns409() throws Exception {
      var body = Map.of("url", "https://example.com/hook");

      // first registration succeeds
      mockMvc
          .perform(
              post("/api/webhooks")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(body)))
          .andExpect(status().isCreated());

      // second registration with same URL must conflict
      mockMvc
          .perform(
              post("/api/webhooks")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(body)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.title").value("Duplicate Webhook"));

      // only one row in DB
      assertThat(webhookRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("failure — missing URL returns 400")
    void registerWebhook_missingUrl_returns400() throws Exception {
      var body = Map.of("description", "No URL provided");

      mockMvc
          .perform(
              post("/api/webhooks")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(body)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors.url").isNotEmpty());
    }

    @Test
    @DisplayName("failure — invalid URL scheme returns 400")
    void registerWebhook_invalidUrlScheme_returns400() throws Exception {
      var body = Map.of("url", "ftp://not-allowed.com/hook");

      mockMvc
          .perform(
              post("/api/webhooks")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(body)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors.url").isNotEmpty());
    }
  }

  // ── GET /api/webhooks ─────────────────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/webhooks")
  class GetAllWebhooks {

    @Test
    @DisplayName("happy path — returns all registered webhooks")
    void getAllWebhooks_happyPath() throws Exception {
      webhookRepository.save(
          Webhook.builder().id(UUID.randomUUID()).url("https://a.com/hook").build());
      webhookRepository.save(
          Webhook.builder().id(UUID.randomUUID()).url("https://b.com/hook").build());

      mockMvc
          .perform(get("/api/webhooks"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("happy path — empty list when no webhooks registered")
    void getAllWebhooks_empty() throws Exception {
      mockMvc
          .perform(get("/api/webhooks"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(0)));
    }
  }

  // ── DELETE /api/webhooks/{id} ─────────────────────────────────────────────

  @Nested
  @DisplayName("DELETE /api/webhooks/{id}")
  class DeleteWebhook {

    @Test
    @DisplayName("happy path — deletes webhook and returns 204")
    void deleteWebhook_happyPath() throws Exception {
      Webhook saved =
          webhookRepository.save(
              Webhook.builder().id(UUID.randomUUID()).url("https://example.com/hook").build());

      mockMvc
          .perform(delete("/api/webhooks/{id}", saved.getId()))
          .andExpect(status().isNoContent());

      assertThat(webhookRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    @DisplayName("failure — unknown ID returns 404")
    void deleteWebhook_notFound_returns404() throws Exception {
      mockMvc
          .perform(delete("/api/webhooks/{id}", UUID.randomUUID()))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.title").value("Webhook Not Found"));
    }
  }
}
