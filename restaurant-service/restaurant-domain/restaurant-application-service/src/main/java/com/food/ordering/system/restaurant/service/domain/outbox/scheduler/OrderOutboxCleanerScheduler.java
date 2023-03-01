package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderOutboxCleanerScheduler implements OutboxScheduler {

	private final OrderOutboxHelper orderOutboxHelper;

	public OrderOutboxCleanerScheduler(OrderOutboxHelper orderOutboxHelper) {
		this.orderOutboxHelper = orderOutboxHelper;
	}

	@Override
	@Scheduled(cron = "@midnight")
	public void processOutboxMessage() {

		Optional<List<OrderOutboxMessage>> completedMessagesOp = orderOutboxHelper
				.getOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
		if (completedMessagesOp.isPresent() && completedMessagesOp.get().size() > 0) {
			orderOutboxHelper.deleteOrderOutboxByOutboxStatus(OutboxStatus.COMPLETED);
			log.info("Deleted {} completed OrderOutboxMessage restaurant approval", completedMessagesOp.get().size());
		}

	}

}
