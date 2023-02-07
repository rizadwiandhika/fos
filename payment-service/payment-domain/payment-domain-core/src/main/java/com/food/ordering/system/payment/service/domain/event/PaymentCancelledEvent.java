package com.food.ordering.system.payment.service.domain.event;

import java.time.ZonedDateTime;
import java.util.Collections;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.payment.service.domain.entity.Payment;

public class PaymentCancelledEvent extends PaymentEvent {

	private final DomainEventPublisher<PaymentCancelledEvent> publisher;

	public PaymentCancelledEvent(Payment payment, ZonedDateTime createdAt,
			DomainEventPublisher<PaymentCancelledEvent> publisher) {
		super(payment, createdAt, Collections.emptyList());
		this.publisher = publisher;
	}

	@Override
	public void fire() {
		publisher.publish(this);

	}

}
