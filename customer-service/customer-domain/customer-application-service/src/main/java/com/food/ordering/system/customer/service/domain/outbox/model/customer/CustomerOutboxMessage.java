package com.food.ordering.system.customer.service.domain.outbox.model.customer;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.food.ordering.system.outbox.OutboxStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CustomerOutboxMessage {

	private final UUID id;
	private final String payload;
	private OutboxStatus outboxStatus;
	private final int version;
	private ZonedDateTime createdAt;
	private ZonedDateTime processedAt;

	public void setOutboxStatus(OutboxStatus outboxStatus) {
		this.outboxStatus = outboxStatus;
	}

	public void setCreatedAt(ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setProcessedAt(ZonedDateTime processedAt) {
		this.processedAt = processedAt;
	}

}
