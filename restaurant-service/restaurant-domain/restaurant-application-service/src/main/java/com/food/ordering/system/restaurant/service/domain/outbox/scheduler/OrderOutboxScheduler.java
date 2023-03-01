package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.RestaurantApprovalResponseMessagePublisher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderOutboxScheduler implements OutboxScheduler {

	private final OrderOutboxHelper orderOutboxHelper;
	private final RestaurantApprovalResponseMessagePublisher restaurantApprovalResponseMessagePublisher;

	public OrderOutboxScheduler(OrderOutboxHelper orderOutboxHelper,
			RestaurantApprovalResponseMessagePublisher restaurantApprovalResponseMessagePublisher) {
		this.orderOutboxHelper = orderOutboxHelper;
		this.restaurantApprovalResponseMessagePublisher = restaurantApprovalResponseMessagePublisher;
	}

	@Override
	@Scheduled(fixedRateString = "${restaurant-service.outbox-scheduler-fixed-rate}", initialDelayString = "${restaurant-service.outbox-scheduler-initial-delay}")
	public void processOutboxMessage() {
		Optional<List<OrderOutboxMessage>> messageOp = orderOutboxHelper
				.getOrderOutboxMessageByOutboxStatus(OutboxStatus.STARTED);
		if (messageOp.isPresent() && messageOp.get().size() > 0) {
			messageOp.get().stream().forEach((message) -> {
				restaurantApprovalResponseMessagePublisher.publish(message, orderOutboxHelper::updateOutboxStatus);
				log.info("Sent message to the restaurant-approval-response for saga id: {}",
						message.getSagaId().toString());
			});
		}
	}

}
