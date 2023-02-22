package com.food.ordering.system.order.service.domain.outbox.scheduler.approval;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.RestaurantApprovalRequestMessagePublisher;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ApprovalOutboxCleanerScheduler implements OutboxScheduler {

	private final ApprovalOutboxHelper approvalOutboxHelper;
	private final RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher;

	public ApprovalOutboxCleanerScheduler(ApprovalOutboxHelper approvalOutboxHelper,
			RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher) {
		this.approvalOutboxHelper = approvalOutboxHelper;
		this.restaurantApprovalRequestMessagePublisher = restaurantApprovalRequestMessagePublisher;
	}

	@Override
	@Scheduled(cron = "@midnight")
	public void processOutboxMessage() {

		Optional<List<OrderApprovalOutboxMessage>> result = approvalOutboxHelper
				.getOrderApprovalOutboxByOutboxStatusAndSagaStatus(OutboxStatus.COMPLETED,
						SagaStatus.COMPENSATED, SagaStatus.FAILED, SagaStatus.SUCCEEDED);

		if (result.isPresent() && result.get().size() > 0) {
			log.info("Deleting {} completed approval outbox messages. Payloads {}",
					result.get().size(),
					result.get().stream().map(OrderApprovalOutboxMessage::getPayload)
							.collect(Collectors.joining("\n")));

			approvalOutboxHelper.deleteApprovalOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus.COMPLETED,
					SagaStatus.COMPENSATED, SagaStatus.FAILED, SagaStatus.SUCCEEDED);

			log.info("Deleted {} completed approval outbox messages", result.get().size());
		}
	}

}
