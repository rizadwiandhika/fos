package com.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.order.SagaConstant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentOutboxHelper {

	private final PaymentOutboxRepository paymentOutboxRepository;
	private final ObjectMapper objectMapper;

	public PaymentOutboxHelper(PaymentOutboxRepository paymentOutboxRepository, ObjectMapper objectMapper) {
		this.paymentOutboxRepository = paymentOutboxRepository;
		this.objectMapper = objectMapper;
	}

	@Transactional(readOnly = true)
	public Optional<List<OrderPaymentOutboxMessage>> getPaymentOutboxMessageByOutboxStatusAndSagaStatus(
			OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
		return paymentOutboxRepository.findByTypeAndOutboxStatusAndSagaStatus(SagaConstant.ORDER_SAGA_NAME,
				outboxStatus, sagaStatus);
	}

	@Transactional(readOnly = true)
	public Optional<OrderPaymentOutboxMessage> getPaymentOutboxMessageBySagaIdAndSagaStatus(
			UUID sagaId, SagaStatus... sagaStatus) {
		return paymentOutboxRepository.findByTypeAndSagaIdAndSagaStatus(SagaConstant.ORDER_SAGA_NAME, sagaId,
				sagaStatus);
	}

	@Transactional
	public void save(OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
		OrderPaymentOutboxMessage result = paymentOutboxRepository.save(orderPaymentOutboxMessage);
		if (result == null) {
			log.error("Unable to save OrderPaymentOutboxMessage with id: {}", orderPaymentOutboxMessage.getId());
			throw new OrderDomainException("Unable to save OrderPaymentOutboxMessage with id: "
					+ orderPaymentOutboxMessage.getId());
		}

		log.info("OrderPaymentOutboxMessage with id: {} is saved", orderPaymentOutboxMessage.getId());

	}

	@Transactional
	void deleteOrderPaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus outboxStatus,
			SagaStatus... sagaStatus) {
		paymentOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatus(SagaConstant.ORDER_SAGA_NAME, outboxStatus,
				sagaStatus);
	}

	@Transactional
	public void savePaymentOutboxMessage(OrderPaymentEventPayload orderPaymentEventPayload, OrderStatus orderStatus,
			SagaStatus sagaStatus, OutboxStatus outboxStatus, UUID sagaId) {
		OrderPaymentOutboxMessage outbox = OrderPaymentOutboxMessage.builder()
				.id(UUID.randomUUID())
				.orderStatus(orderStatus)
				.sagaStatus(sagaStatus)
				.outboxStatus(outboxStatus)
				.sagaId(sagaId)
				.type(SagaConstant.ORDER_SAGA_NAME)
				.payload(createPayload(orderPaymentEventPayload))
				.createdAt(orderPaymentEventPayload.getCreatedAt())
				.build();

		save(outbox);
	}

	private String createPayload(OrderPaymentEventPayload orderPaymentEventPayload) {
		try {
			return objectMapper.writeValueAsString(orderPaymentEventPayload);
		} catch (JsonProcessingException e) {
			log.error("Unable to create payload for OrderPaymentOutboxMessage with id: {}",
					orderPaymentEventPayload.getOrderId());
			throw new OrderDomainException("Unable to create payload for OrderPaymentOutboxMessage with id: "
					+ orderPaymentEventPayload.getOrderId());
		}
	}
}
