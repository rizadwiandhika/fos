package com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.mapper;

import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.entity.ApprovalOutboxEntity;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;

@Component
public class ApprovalOutboxDataMapper {
	public ApprovalOutboxEntity orderCreateOutboxMessageToOutboxEntity(OrderApprovalOutboxMessage outbox) {
		return ApprovalOutboxEntity.builder()
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

	public OrderApprovalOutboxMessage outboxEntityToOrderCreateOutboxMessage(ApprovalOutboxEntity entity) {
		return OrderApprovalOutboxMessage.builder()
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
