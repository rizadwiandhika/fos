package com.food.ordering.system.order.service.dataaccess.outbox.payment.mapper;

import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.dataaccess.outbox.payment.entity.PaymentOutboxEntity;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;

@Component
public class PaymentOutboxDataAccessMapper {
	public PaymentOutboxEntity orderPaymentOutboxMessagToOutboxEntity(OrderPaymentOutboxMessage outbox) {
		return PaymentOutboxEntity.builder()
				.id(outbox.getId())
				.sagaId(outbox.getSagaId())
				.createdAt(outbox.getCreatedAt())
				.type(outbox.getType())
				.payload(outbox.getPayload())
				.orderStatus(outbox.getOrderStatus())
				.sagaStatus(outbox.getSagaStatus())
				.outboxStatus(outbox.getOutboxStatus())
				.version(outbox.getVersion())
				.build();
	}

	public OrderPaymentOutboxMessage outboxEntityToOrderPaymentOutboxMessage(PaymentOutboxEntity entity) {
		return OrderPaymentOutboxMessage.builder()
				.id(entity.getId())
				.sagaId(entity.getSagaId())
				.createdAt(entity.getCreatedAt())
				.type(entity.getType())
				.payload(entity.getPayload())
				.orderStatus(entity.getOrderStatus())
				.sagaStatus(entity.getSagaStatus())
				.outboxStatus(entity.getOutboxStatus())
				.version(entity.getVersion())
				.build();
	}
}
