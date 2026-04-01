package com.run.simple.payment.repository;

import com.run.simple.payment.model.DeliveryStatus;
import com.run.simple.payment.model.WebhookDeliveryLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookDeliveryLogRepository extends CrudRepository<WebhookDeliveryLog, UUID> {

  /** All delivery attempts for a given payment — useful for audit/debugging. */
  List<WebhookDeliveryLog> findByPaymentId(UUID paymentId);

  /** All attempts for a specific webhook registration. */
  List<WebhookDeliveryLog> findByWebhookId(UUID webhookId);

  /** Fetch logs by status — e.g. find all FAILED entries for monitoring. */
  List<WebhookDeliveryLog> findByStatus(DeliveryStatus status);

  /** Count attempts already made for a (webhook, payment) pair — used by retry logic. */
  int countByWebhookIdAndPaymentId(UUID webhookId, UUID paymentId);
}
