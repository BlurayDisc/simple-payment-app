package com.run.simple.payment.repository;

import com.run.simple.payment.model.Webhook;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebhookRepository extends CrudRepository<Webhook, UUID> {

  /** Used to enforce the unique URL constraint at the service layer before hitting the DB. */
  Optional<Webhook> findByUrl(String url);
}
