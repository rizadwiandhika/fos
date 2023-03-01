package com.food.ordering.system.payment.service.domain.outbox.scheduler;

import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderOutboxCleaner implements OutboxScheduler {

	private OrderOutboxHelper orderOutboxHelper;

	public OrderOutboxCleaner(OrderOutboxHelper orderOutboxHelper) {
		this.orderOutboxHelper = orderOutboxHelper;
	}

	@Override
	@Transactional
	@Scheduled(cron = "@midnight")
	public void processOutboxMessage() {
		Optional<List<OrderOutboxMessage>> op = orderOutboxHelper
				.getOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
		if (op.isPresent() && op.get().size() > 0) {
			orderOutboxHelper.deleteByOutboxStatus(OutboxStatus.COMPLETED);
			log.info("OrderOutboxCleaner: {} messages have been cleaned up!", op.get().size());
		}
	}

}
