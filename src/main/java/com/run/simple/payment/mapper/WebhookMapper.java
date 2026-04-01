package com.run.simple.payment.mapper;

import com.run.simple.payment.dto.WebhookResponse;
import com.run.simple.payment.model.Webhook;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WebhookMapper {

  WebhookResponse toResponse(Webhook webhook);
}
