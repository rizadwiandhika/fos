package com.food.ordering.system.payment.service.domain.ports.output.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;

public interface OrderOutboxRepository {
	OrderOutboxMessage save(OrderOutboxMessage outbox);

	Optional<List<OrderOutboxMessage>> findByTypeAndOutboxStatus(
			String type,
			OutboxStatus outboxStatus);

	Optional<OrderOutboxMessage> findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(
			String type,
			UUID sagaId,
			PaymentStatus paymentStatus,
			OutboxStatus outboxStatus);

	void deleteByTypeAndOutboxStatus(
			String type,
			OutboxStatus outboxStatus);
}
