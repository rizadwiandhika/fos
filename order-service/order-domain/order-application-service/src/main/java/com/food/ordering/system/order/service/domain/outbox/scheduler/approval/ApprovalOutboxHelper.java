package com.food.ordering.system.order.service.domain.outbox.scheduler.approval;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.repository.ApprovalOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.order.SagaConstant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ApprovalOutboxHelper {

	private final ApprovalOutboxRepository approvalOutboxRepository;
	private final ObjectMapper objectMapper;

	public ApprovalOutboxHelper(ApprovalOutboxRepository approvalOutboxRepository, ObjectMapper objectMapper) {
		this.approvalOutboxRepository = approvalOutboxRepository;
		this.objectMapper = objectMapper;
	}

	@Transactional(readOnly = true)
	public Optional<List<OrderApprovalOutboxMessage>> getOrderApprovalOutboxByOutboxStatusAndSagaStatus(
			OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
		return approvalOutboxRepository.findByTypeAndOutboxStatusAndSagaStatus(SagaConstant.ORDER_SAGA_NAME,
				outboxStatus, sagaStatus);
	}

	@Transactional(readOnly = true)
	public Optional<OrderApprovalOutboxMessage> getOrderApprovalOutboxBySagaIdAndSagaStatus(
			UUID sagaId, SagaStatus... sagaStatus) {
		return approvalOutboxRepository.findByTypeAndSagaIdAndSagaStatus(SagaConstant.ORDER_SAGA_NAME,
				sagaId, sagaStatus);
	}

	@Transactional
	public void save(OrderApprovalOutboxMessage outboxMessage) {
		OrderApprovalOutboxMessage result = approvalOutboxRepository.save(outboxMessage);
		if (result == null) {
			log.error("Unable to save OrderApprovalOutboxMessage with id: {}", outboxMessage.getId());
			throw new RuntimeException("Unable to save OrderApprovalOutboxMessage with id: "
					+ outboxMessage.getId());
		}

		log.info("OrderApprovalOutboxMessage with id: {} is saved", outboxMessage.getId());
	}

	public void deleteApprovalOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus outboxStatus,
			SagaStatus... sagaStatus) {
		approvalOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatus(SagaConstant.ORDER_SAGA_NAME, outboxStatus,
				sagaStatus);
		;
	}

	@Transactional
	public void saveApprovalOutboxMessage(OrderApprovalEventPayload orderApprovalEventPayload,
			OrderStatus orderStatus,
			SagaStatus sagaStatus,
			OutboxStatus outboxStatus,
			UUID sagaId) {
		OrderApprovalOutboxMessage outboxMessage = OrderApprovalOutboxMessage.builder()
				.id(UUID.randomUUID())
				.sagaId(sagaId)
				.createdAt(orderApprovalEventPayload.getCreatedAt())
				.type(SagaConstant.ORDER_SAGA_NAME)
				.payload(createPayload(orderApprovalEventPayload))
				.orderStatus(orderStatus)
				.sagaStatus(sagaStatus)
				.outboxStatus(outboxStatus)
				.build();

		save(outboxMessage);
	}

	private String createPayload(OrderApprovalEventPayload orderApprovalEventPayload) {
		try {
			return objectMapper.writeValueAsString(orderApprovalEventPayload);
		} catch (JsonProcessingException e) {
			log.error("Unable to create payload for OrderApprovalOutboxMessage. Order id: {}",
					orderApprovalEventPayload.getOrderId(), e);
			throw new OrderDomainException("Unable to create payload for OrderApprovalOutboxMessage. Order id: "
					+ orderApprovalEventPayload.getOrderId());
		}
	}

}
