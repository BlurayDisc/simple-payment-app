package com.run.simple.payment.mapper;

import com.run.simple.payment.dto.PaymentResponse;
import com.run.simple.payment.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

  /**
   * Maps a {@link Payment} entity to a {@link PaymentResponse} DTO.
   * cardNumberEnc is intentionally excluded — raw or encrypted PAN
   * must never leak into API responses.
   */
  @Mapping(target = "cardLastFour", source = "cardLastFour")
  PaymentResponse toResponse(Payment payment);
}
