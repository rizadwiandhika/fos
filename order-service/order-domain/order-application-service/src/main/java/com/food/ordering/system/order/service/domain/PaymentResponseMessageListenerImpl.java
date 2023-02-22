package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@Service
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {

	private static final CharSequence DELIMITER = ";";
	private final OrderPaymentSaga orderPaymentSaga;

	public PaymentResponseMessageListenerImpl(OrderPaymentSaga orderPaymentSaga) {
		this.orderPaymentSaga = orderPaymentSaga;
	}

	@Override
	public void paymentCancelled(PaymentResponse paymentResponse) {
		orderPaymentSaga.rollback(paymentResponse);
		log.info("Order id: {} is rolled back with failure messages: {}", paymentResponse.getOrderId(),
				String.join(DELIMITER, paymentResponse.getFailureMessages()));
	}

	@Override
	public void paymentCompleted(PaymentResponse paymentResponse) {
		OrderPaidEvent domainEvent = orderPaymentSaga.process(paymentResponse);

		log.info("Publishing OrderPaidEvent for order id: {} ", paymentResponse.getOrderId());
		domainEvent.fire();
	}

}
