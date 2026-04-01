package com.run.simple.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.run.simple.payment.BaseIntegrationTest;
import com.run.simple.payment.model.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Payment API")
class PaymentControllerIT extends BaseIntegrationTest {

  @Autowired ObjectMapper objectMapper;

  // ── POST /api/payments ────────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/payments")
  class CreatePayment {

    @Test
    @DisplayName("happy path — creates payment and returns 201 with masked card")
    void createPayment_happyPath() throws Exception {
      var body =
          Map.of(
              "firstName", "John",
              "lastName", "Doe",
              "zipCode", "10001",
              "cardNumber", "4111111111111111");

      mockMvc
          .perform(
              post("/api/payments")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(body)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").isNotEmpty())
          .andExpect(jsonPath("$.firstName").value("John"))
          .andExpect(jsonPath("$.lastName").value("Doe"))
          .andExpect(jsonPath("$.zipCode").value("10001"))
          .andExpect(jsonPath("$.cardLastFour").value("1111"))
          .andExpect(jsonPath("$.createdAt").isNotEmpty())
          // raw card number must NEVER appear in the response
          .andExpect(jsonPath("$.cardNumber").doesNotExist())
          .andExpect(jsonPath("$.cardNumberEnc").doesNotExist());

      // verify row persisted with encrypted (not plaintext) card
      Iterable<Payment> saved = paymentRepository.findAll();
      assertThat(saved).hasSize(1);
      Payment payment = saved.iterator().next();
      assertThat(payment.getCardNumberEnc()).isNotEqualTo("4111111111111111");
      assertThat(payment.getCardLastFour()).isEqualTo("1111");
    }

    @Test
    @DisplayName("failure — missing required fields returns 400 with field errors")
    void createPayment_missingFields_returns400() throws Exception {
      var body = Map.of("firstName", "John"); // lastName, zipCode, cardNumber missing

      mockMvc
          .perform(
              post("/api/payments")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(body)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.title").value("Validation Failed"))
          .andExpect(jsonPath("$.errors.lastName").isNotEmpty())
          .andExpect(jsonPath("$.errors.zipCode").isNotEmpty())
          .andExpect(jsonPath("$.errors.cardNumber").isNotEmpty());
    }

    @Test
    @DisplayName("failure — invalid card number format returns 400")
    void createPayment_invalidCardNumber_returns400() throws Exception {
      var body =
          Map.of(
              "firstName", "John",
              "lastName", "Doe",
              "zipCode", "10001",
              "cardNumber", "not-a-card");

      mockMvc
          .perform(
              post("/api/payments")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(body)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors.cardNumber").isNotEmpty());
    }

    @Test
    @DisplayName("failure — invalid zip code format returns 400")
    void createPayment_invalidZipCode_returns400() throws Exception {
      var body =
          Map.of(
              "firstName", "John",
              "lastName", "Doe",
              "zipCode", "ABCDE",
              "cardNumber", "4111111111111111");

      mockMvc
          .perform(
              post("/api/payments")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(body)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors.zipCode").isNotEmpty());
    }
  }

  // ── GET /api/payments/{id} ────────────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/payments/{id}")
  class GetPayment {

    @Test
    @DisplayName("happy path — returns existing payment")
    void getPayment_happyPath() throws Exception {
      // seed a payment via the API
      var body =
          Map.of(
              "firstName", "Jane",
              "lastName", "Smith",
              "zipCode", "90210",
              "cardNumber", "5500005555555559");

      String response =
          mockMvc
              .perform(
                  post("/api/payments")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(body)))
              .andExpect(status().isCreated())
              .andReturn()
              .getResponse()
              .getContentAsString();

      String id = objectMapper.readTree(response).get("id").asText();

      mockMvc
          .perform(get("/api/payments/{id}", id))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(id))
          .andExpect(jsonPath("$.firstName").value("Jane"))
          .andExpect(jsonPath("$.cardLastFour").value("5559"));
    }

    @Test
    @DisplayName("failure — unknown ID returns 404")
    void getPayment_notFound_returns404() throws Exception {
      mockMvc
          .perform(get("/api/payments/{id}", UUID.randomUUID()))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.title").value("Payment Not Found"));
    }
  }

  // ── GET /api/payments ─────────────────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/payments")
  class GetAllPayments {

    @Test
    @DisplayName("happy path — returns all payments")
    void getAllPayments_happyPath() throws Exception {
      // create two payments
      for (String card : new String[] {"4111111111111111", "5500005555555559"}) {
        mockMvc
            .perform(
                post("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of(
                                "firstName",
                                "Test",
                                "lastName",
                                "User",
                                "zipCode",
                                "10001",
                                "cardNumber",
                                card))))
            .andExpect(status().isCreated());
      }

      mockMvc
          .perform(get("/api/payments"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("happy path — returns empty list when no payments exist")
    void getAllPayments_empty_returnsEmptyList() throws Exception {
      mockMvc
          .perform(get("/api/payments"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(0)));
    }
  }
}
