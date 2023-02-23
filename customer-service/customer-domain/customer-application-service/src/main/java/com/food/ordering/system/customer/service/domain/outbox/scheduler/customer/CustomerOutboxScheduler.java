package com.food.ordering.system.customer.service.domain.outbox.scheduler.customer;

import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.customer.service.domain.outbox.model.customer.CustomerOutboxMessage;
import com.food.ordering.system.customer.service.domain.ports.output.message.CustomerMessagePublisher;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomerOutboxScheduler implements OutboxScheduler {

	private final CustomerOutboxHelper customerOutboxHelper;
	private final CustomerMessagePublisher customerMessagePublisher;

	public CustomerOutboxScheduler(CustomerOutboxHelper customerOutboxHelper,
			CustomerMessagePublisher customerMessagePublisher) {
		this.customerOutboxHelper = customerOutboxHelper;
		this.customerMessagePublisher = customerMessagePublisher;
	}

	@Override
	@Transactional
	@Scheduled(initialDelayString = "${customer-service.outbox-scheduler-initial-delay}", fixedRateString = "${customer-service.outbox-scheduler-fixed-rate}")
	public void processOutboxMessage() {
		Optional<List<CustomerOutboxMessage>> outboxMessages = customerOutboxHelper
				.getCustomerOutboxByOutboxStatus(OutboxStatus.STARTED);
		if (outboxMessages.isPresent() && outboxMessages.get().size() > 0) {
			outboxMessages.get().forEach(this::publishMessage);
		}
		log.info("Sent {} customer outbox(s) to the bus!", outboxMessages.get().size());
	}

	void publishMessage(CustomerOutboxMessage message) {
		customerMessagePublisher.publish(message, customerOutboxHelper::updateCustomerOutbox);
	}

}
