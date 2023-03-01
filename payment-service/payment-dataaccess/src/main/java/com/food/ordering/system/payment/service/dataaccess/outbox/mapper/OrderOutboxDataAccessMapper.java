package com.food.ordering.system.payment.service.dataaccess.outbox.mapper;

import org.springframework.stereotype.Component;

import com.food.ordering.system.payment.service.dataaccess.outbox.entity.OrderOutboxEntity;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;

@Component
public class OrderOutboxDataAccessMapper {
	public OrderOutboxEntity orderOutboxMessageToOutboxEntity(OrderOutboxMessage message) {
		return OrderOutboxEntity.builder()
				.id(message.getId())
				.sagaId(message.getSagaId())
				.createdAt(message.getCreatedAt())
				.processedAt(message.getProcessedAt())
				.type(message.getType())
				.payload(message.getPayload())
				.outboxStatus(message.getOutboxStatus())
				.paymentStatus(message.getPaymentStatus())
				.version(message.getVersion())
				.build();

	}

	public OrderOutboxMessage orderOutboxEntityToOrderOutboxMessage(OrderOutboxEntity entity) {
		return OrderOutboxMessage.builder()
				.id(entity.getId())
				.sagaId(entity.getSagaId())
				.createdAt(entity.getCreatedAt())
				.processedAt(entity.getProcessedAt())
				.type(entity.getType())
				.payload(entity.getPayload())
				.outboxStatus(entity.getOutboxStatus())
				.paymentStatus(entity.getPaymentStatus())
				.version(entity.getVersion())
				.build();
	}

}
