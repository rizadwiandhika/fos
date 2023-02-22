package com.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentOutboxCleanerScheduler implements OutboxScheduler {

	private final PaymentOutboxHelper paymentOutboxHelper;

	public PaymentOutboxCleanerScheduler(PaymentOutboxHelper paymentOutboxHelper) {
		this.paymentOutboxHelper = paymentOutboxHelper;
	}

	@Override
	@Scheduled(cron = "@midnight" /* , zone = "Asia/Kolkata" */)
	public void processOutboxMessage() {
		Optional<List<OrderPaymentOutboxMessage>> result = paymentOutboxHelper
				.getPaymentOutboxMessageByOutboxStatusAndSagaStatus(
						OutboxStatus.COMPLETED,
						SagaStatus.SUCCEEDED,
						SagaStatus.FAILED,
						SagaStatus.COMPENSATED);

		if (result.isPresent() && result.get().size() > 0) {
			List<OrderPaymentOutboxMessage> messages = result.get();
			log.info("Recieved {} OrderPaymentOutboxMessage for clean-up. The payloads", messages.size(),
					messages.stream().map(OrderPaymentOutboxMessage::getPayload).collect(Collectors.joining("\n")));

			paymentOutboxHelper.deleteOrderPaymentOutboxMessageByOutboxStatusAndSagaStatus(
					OutboxStatus.COMPLETED,
					SagaStatus.SUCCEEDED,
					SagaStatus.FAILED,
					SagaStatus.COMPENSATED);

			log.info("OrderPaymentOutboxMessage with ids: {} have been deleted!", messages.size(),
					messages.stream().map(m -> m.getId().toString()).collect(Collectors.joining(",")));
		}
	}

}
