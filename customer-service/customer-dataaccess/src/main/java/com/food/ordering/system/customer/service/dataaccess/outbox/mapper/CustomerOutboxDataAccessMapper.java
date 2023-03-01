package com.food.ordering.system.customer.service.dataaccess.outbox.mapper;

import org.springframework.stereotype.Component;

import com.food.ordering.system.customer.service.dataaccess.outbox.entity.CustomerOutboxEntity;
import com.food.ordering.system.customer.service.domain.outbox.model.customer.CustomerOutboxMessage;

@Component
public class CustomerOutboxDataAccessMapper {

	public CustomerOutboxEntity customerOutboxMessageToCustomerOutboxEntity(CustomerOutboxMessage outbox) {
		return CustomerOutboxEntity.builder()
				.id(outbox.getId())
				.payload(outbox.getPayload())
				.outboxStatus(outbox.getOutboxStatus())
				.version(outbox.getVersion())
				.createdAt(outbox.getCreatedAt())
				.processedAt(outbox.getProcessedAt())
				.build();
	}

	public CustomerOutboxMessage CustomerOutboxEntityToCustomerOutboxMessage(CustomerOutboxEntity entity) {
		return CustomerOutboxMessage.builder()
				.id(entity.getId())
				.payload(entity.getPayload())
				.outboxStatus(entity.getOutboxStatus())
				.version(entity.getVersion())
				.createdAt(entity.getCreatedAt())
				.processedAt(entity.getProcessedAt())
				.build();
	}

}
