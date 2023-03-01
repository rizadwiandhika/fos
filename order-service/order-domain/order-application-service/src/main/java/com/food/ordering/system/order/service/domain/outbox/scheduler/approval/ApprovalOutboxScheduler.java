package com.food.ordering.system.order.service.domain.outbox.scheduler.approval;

import java.util.Optional;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.RestaurantApprovalRequestMessagePublisher;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ApprovalOutboxScheduler implements OutboxScheduler {

	private final ApprovalOutboxHelper approvalOutboxHelper;
	private final RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher;

	public ApprovalOutboxScheduler(ApprovalOutboxHelper approvalOutboxHelper,
			RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher) {
		this.approvalOutboxHelper = approvalOutboxHelper;
		this.restaurantApprovalRequestMessagePublisher = restaurantApprovalRequestMessagePublisher;
	}

	@Override
	@Transactional
	@Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}", initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
	public void processOutboxMessage() {
		Optional<List<OrderApprovalOutboxMessage>> result = approvalOutboxHelper
				.getOrderApprovalOutboxByOutboxStatusAndSagaStatus(OutboxStatus.STARTED,
						SagaStatus.PROCESSING);

		if (result.isPresent() && result.get().size() > 0) {
			result.get().forEach(outboxMessage -> {
				restaurantApprovalRequestMessagePublisher.publish(outboxMessage, this::updateOutboxMessage);
			});
		}
	}

	private void updateOutboxMessage(OrderApprovalOutboxMessage outboxMessage, OutboxStatus outboxStatus) {
		outboxMessage.setOutboxStatus(outboxStatus);
		approvalOutboxHelper.save(outboxMessage);
	}

}
