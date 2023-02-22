package com.food.ordering.system.payment.service.domain.outbox.scheduler;

import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderOutboxScheduler implements OutboxScheduler {

	private OrderOutboxHelper orderOutboxHelper;
	private PaymentResponseMessagePublisher paymentResponseMessagePublisher;

	public OrderOutboxScheduler(OrderOutboxHelper orderOutboxHelper,
			PaymentResponseMessagePublisher paymentResponseMessagePublisher) {
		this.orderOutboxHelper = orderOutboxHelper;
		this.paymentResponseMessagePublisher = paymentResponseMessagePublisher;
	}

	@Override
	@Scheduled(fixedRateString = "${payment-service.outbox-scheduler-fixed-rate}", initialDelayString = "${payment-service.outbox-scheduler-initial-delay}")
	@Transactional
	public void processOutboxMessage() {
		Optional<List<OrderOutboxMessage>> op = orderOutboxHelper
				.getOrderOutboxMessageByOutboxStatus(OutboxStatus.STARTED);

		if (op.isPresent() && op.get().size() > 0) {
			List<OrderOutboxMessage> orderOutboxMessages = op.get();

			orderOutboxMessages.forEach(outboxMessage -> {
				paymentResponseMessagePublisher.publish(outboxMessage, orderOutboxHelper::updatePaymentOutboxMessage);
			});

			log.info("OrderOutboxScheduler: {} messages have been processed!", orderOutboxMessages.size());
		}

	}
}
