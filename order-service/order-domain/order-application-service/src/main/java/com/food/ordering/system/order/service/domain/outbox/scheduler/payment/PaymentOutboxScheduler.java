package com.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentOutboxScheduler implements OutboxScheduler {

	private final PaymentOutboxHelper paymentOutboxHelper;
	private final PaymentRequestMessagePublisher paymentRequestMessagePublisher;

	public PaymentOutboxScheduler(PaymentOutboxHelper paymentOutboxHelper,
			PaymentRequestMessagePublisher paymentRequestMessagePublisher) {
		this.paymentOutboxHelper = paymentOutboxHelper;
		this.paymentRequestMessagePublisher = paymentRequestMessagePublisher;
	}

	@Override
	@Transactional
	@Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}", initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
	public void processOutboxMessage() {
		Optional<List<OrderPaymentOutboxMessage>> result = paymentOutboxHelper
				.getPaymentOutboxMessageByOutboxStatusAndSagaStatus(
						OutboxStatus.STARTED,
						SagaStatus.STARTED,
						SagaStatus.COMPENSATING);

		if (result.isPresent() && result.get().size() > 0) {
			List<OrderPaymentOutboxMessage> messages = result.get();
			log.info("Recieved {} OrderPaymentOutboxMessage with ids: {}", messages.size(),
					messages.stream().map(m -> m.getId().toString()).collect(Collectors.joining(",")));

			messages.forEach(message -> {
				paymentRequestMessagePublisher.publish(message, this::updateOutboxStatus);
			});

			log.info("OrderPaymentOutboxMessage with ids: {} have been sent to message bus!", messages.size(),
					messages.stream().map(m -> m.getId().toString()).collect(Collectors.joining(",")));
		}

	}

	private void updateOutboxStatus(OrderPaymentOutboxMessage m, OutboxStatus status) {
		m.setOutboxStatus(status);
		paymentOutboxHelper.save(m);
	}

}
