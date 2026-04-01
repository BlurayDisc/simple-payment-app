package com.run.simple.payment.repository;

import com.run.simple.payment.model.Payment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, UUID> {
  // CrudRepository provides: save, findById, findAll, deleteById, etc.
  // Add custom query methods here if needed in future phases.
}
