package com.food.ordering.system.payment.service.domain.outbox.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderOutboxMessage {

	private UUID id;
	private UUID sagaId;
	private ZonedDateTime createdAt;
	private ZonedDateTime processedAt;
	private String type;
	private String payload;
	private PaymentStatus paymentStatus;
	private OutboxStatus outboxStatus;
	private int version;

	public void setOutboxStatus(OutboxStatus outboxStatus) {
		this.outboxStatus = outboxStatus;
	}
}
