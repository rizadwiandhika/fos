package com.food.ordering.system.payment.service.domain;

import org.springframework.stereotype.Service;

import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentRequestMessageListenerImpl implements PaymentRequestMessageListener {

	private final PaymentRequestHelper paymentRequestHelper;

	public PaymentRequestMessageListenerImpl(PaymentRequestHelper paymentRequestHelper) {
		this.paymentRequestHelper = paymentRequestHelper;
	}

	@Override
	public void cancelPayment(PaymentRequest paymentRequest) {
		PaymentEvent paymentEvent = paymentRequestHelper.persistCancelPayment(paymentRequest);
		fireEvent(paymentEvent);
	}

	@Override
	public void completePayment(PaymentRequest paymentRequest) {
		PaymentEvent paymentEvent = paymentRequestHelper.persistPayment(paymentRequest);
		fireEvent(paymentEvent);
	}

	private void fireEvent(PaymentEvent paymentEvent) {
		String message = String.format("Publishing payment event with payemnt id: %s and order id: %s\n",
				paymentEvent.getPayment().getId().getValue().toString(),
				paymentEvent.getPayment().getOrderId().getValue().toString());

		log.info(message);

		paymentEvent.fire();
	}

}
