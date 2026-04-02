package com.run.simple.payment.repository;

import com.run.simple.payment.model.Payment;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** CrudRepository providing: save, findById, findAll, deleteById, etc for Payments. */
@Repository
public interface PaymentRepository extends CrudRepository<Payment, UUID> {}
