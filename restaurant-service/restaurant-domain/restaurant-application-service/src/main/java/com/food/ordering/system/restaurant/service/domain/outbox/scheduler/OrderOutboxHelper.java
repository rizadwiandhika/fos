package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import static com.food.ordering.system.domain.DomainConstants.UTC;
import static com.food.ordering.system.outbox.OutboxStatus.STARTED;
import static com.food.ordering.system.saga.order.SagaConstant.ORDER_SAGA_NAME;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueObject.OrderApprovalStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantDomainException;
import com.food.ordering.system.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderOutboxRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderOutboxHelper {

	private final OrderOutboxRepository orderOutboxRepository;
	private final ObjectMapper objectMapper;
	private final RestaurantDataMapper restaurantDataMapper;

	public OrderOutboxHelper(OrderOutboxRepository orderOutboxRepository, ObjectMapper objectMapper,
			RestaurantDataMapper restaurantDataMapper) {
		this.orderOutboxRepository = orderOutboxRepository;
		this.objectMapper = objectMapper;
		this.restaurantDataMapper = restaurantDataMapper;
	}

	@Transactional(readOnly = true)
	public Optional<OrderOutboxMessage> getCompletedOrderOutboxMessageBySagaIdAndOutboxStatus(UUID sagaId,
			OutboxStatus outboxStatus) {
		return orderOutboxRepository.findByTypeAndSagaIdAndOutboxStatus(ORDER_SAGA_NAME, sagaId, outboxStatus);
	}

	@Transactional(readOnly = true)
	public Optional<List<OrderOutboxMessage>> getOrderOutboxMessageByOutboxStatus(
			OutboxStatus outboxStatus) {
		return orderOutboxRepository.findByTypeAndOutboxStatus(ORDER_SAGA_NAME, outboxStatus);
	}

	@Transactional
	public void deleteOrderOutboxByOutboxStatus(OutboxStatus outboxStatus) {
		orderOutboxRepository.deleteByTypeAndoutboxStatus(ORDER_SAGA_NAME, outboxStatus);
	}

	@Transactional
	public void updateOutboxStatus(OrderOutboxMessage outboxMessage, OutboxStatus outboxStatus) {
		outboxMessage.setOutboxStatus(outboxStatus);
		save(outboxMessage);
	}

	@Transactional
	public void saveOrderOutboxMessage(OrderEventPayload payload, String sagaId,
			OutboxStatus outboxStatus) {
		OrderOutboxMessage outboxMessage = OrderOutboxMessage.builder()
				.id(UUID.randomUUID())
				.sagaId(UUID.fromString(sagaId))
				.createdAt(payload.getCreatedAt())
				.processedAt(ZonedDateTime.now(ZoneId.of(UTC)))
				.type(ORDER_SAGA_NAME)
				.payload(parsePayload(payload))
				.outboxStatus(outboxStatus)
				.approvalStatus(OrderApprovalStatus.valueOf(payload.getOrderApprovalStatus()))
				.build();

		save(outboxMessage);
	}

	private String parsePayload(OrderEventPayload orderEventPayload) {
		try {
			return objectMapper.writeValueAsString(orderEventPayload);
		} catch (JsonProcessingException e) {
			log.error("Unable to write OrderEventPayload into JSON", e);
			throw new RestaurantDomainException("Unable to write OrderEventPayload into JSON");
		}
	}

	private void save(OrderOutboxMessage outboxMessage) {
		OrderOutboxMessage result = orderOutboxRepository.save(outboxMessage);
		if (result == null) {
			throw new RestaurantDomainException(
					"Unable to save OurderOutboxMessage for saga id: " + outboxMessage.getSagaId().toString());
		}
	}

}
