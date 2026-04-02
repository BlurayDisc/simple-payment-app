package com.run.simple.payment.service;

import com.run.simple.payment.dto.CreateWebhookRequest;
import com.run.simple.payment.dto.WebhookResponse;
import com.run.simple.payment.exception.DuplicateWebhookException;
import com.run.simple.payment.exception.WebhookNotFoundException;
import com.run.simple.payment.mapper.WebhookMapper;
import com.run.simple.payment.model.Webhook;
import com.run.simple.payment.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

  private final WebhookRepository webhookRepository;
  private final WebhookMapper webhookMapper;

  public WebhookResponse registerWebhook(CreateWebhookRequest request) {
    // Enforce unique URL at service layer before hitting the DB unique index
    webhookRepository
        .findByUrl(request.getUrl())
        .ifPresent(
            existing -> {
              throw new DuplicateWebhookException(request.getUrl());
            });

    Webhook webhook =
        Webhook.builder()
            .url(request.getUrl())
            .description(request.getDescription())
            .build();

    Webhook saved = webhookRepository.save(webhook);
    log.info("Webhook registered: id={} url={}", saved.getId(), saved.getUrl());
    return webhookMapper.toResponse(saved);
  }

  public List<WebhookResponse> getAllWebhooks() {
    return StreamSupport.stream(webhookRepository.findAll().spliterator(), false)
        .map(webhookMapper::toResponse)
        .toList();
  }

  public void deleteWebhook(UUID id) {
    if (!webhookRepository.existsById(id)) {
      throw new WebhookNotFoundException(id);
    }
    webhookRepository.deleteById(id);
    log.info("Webhook deleted: id={}", id);
  }
}
