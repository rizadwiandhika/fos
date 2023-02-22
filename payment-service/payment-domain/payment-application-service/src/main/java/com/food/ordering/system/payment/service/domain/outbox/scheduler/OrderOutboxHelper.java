package com.food.ordering.system.payment.service.domain.outbox.scheduler;

import static com.food.ordering.system.domain.DomainConstants.UTC;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.exception.PaymentDomainException;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.ports.output.repository.OrderOutboxRepository;
import com.food.ordering.system.saga.order.SagaConstant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderOutboxHelper {

	private final OrderOutboxRepository orderOutboxRepository;
	private final ObjectMapper objectMapper;

	public OrderOutboxHelper(OrderOutboxRepository orderOutboxRepository, ObjectMapper objectMapper) {
		this.orderOutboxRepository = orderOutboxRepository;
		this.objectMapper = objectMapper;
	}

	@Transactional(readOnly = true)
	public Optional<OrderOutboxMessage> getCompletedOrderOutboxMessageBySagaIdAndPaymentStatus(
			UUID sagaId,
			PaymentStatus paymentStatus) {
		return orderOutboxRepository
				.findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(SagaConstant.ORDER_SAGA_NAME, sagaId, paymentStatus,
						OutboxStatus.COMPLETED);

	}

	@Transactional(readOnly = true)
	public Optional<List<OrderOutboxMessage>> getOrderOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
		return orderOutboxRepository.findByTypeAndOutboxStatus(SagaConstant.ORDER_SAGA_NAME, outboxStatus);
	}

	@Transactional
	public void deleteByOutboxStatus(OutboxStatus outboxStatus) {
		orderOutboxRepository.deleteByTypeAndOutboxStatus(SagaConstant.ORDER_SAGA_NAME, outboxStatus);
	}

	@Transactional
	private OrderOutboxMessage save(OrderOutboxMessage orderOutboxMessage) {
		OrderOutboxMessage result = orderOutboxRepository.save(orderOutboxMessage);
		if (result == null) {
			log.error("Unable to save order outbox message. Saga id: {}", orderOutboxMessage.getSagaId().toString());
			throw new PaymentDomainException("Unable to save order outbox message. Saga id: "
					+ orderOutboxMessage.getSagaId().toString());
		}
		return result;
	}

	@Transactional
	public OrderOutboxMessage saveOrderOutboxMessage(OrderEventPayload eventPayload, PaymentStatus paymentStatus,
			OutboxStatus outboxStatus, UUID sagaId) {
		OrderOutboxMessage orderOutboxMessage = OrderOutboxMessage.builder()
				.id(UUID.randomUUID())
				.sagaId(sagaId)
				.createdAt(eventPayload.getCreatedAt())
				.processedAt(ZonedDateTime.now(ZoneId.of(UTC)))
				.type(SagaConstant.ORDER_SAGA_NAME)
				.payload(getPayload(eventPayload))
				.paymentStatus(paymentStatus)
				.outboxStatus(outboxStatus)
				.build();

		return save(orderOutboxMessage);
	}

	@Transactional
	public void updatePaymentOutboxMessage(OrderOutboxMessage outbox, OutboxStatus outboxStatus) {
		outbox.setOutboxStatus(outboxStatus);
		save(outbox);
		log.info("Order outbox status is updated as: ", outboxStatus.name());
	}

	private String getPayload(OrderEventPayload eventPayload) {
		try {
			return objectMapper.writeValueAsString(eventPayload);
		} catch (JsonProcessingException e) {
			log.error("Unable to parse event payload", e);
			throw new PaymentDomainException("Unable to parse event payload", e);
		}
	}

}
