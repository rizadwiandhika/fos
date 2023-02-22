package com.food.ordering.system.payment.service.domain.event;

import java.time.ZonedDateTime;
import java.util.Collections;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.payment.service.domain.entity.Payment;

public class PaymentCompletedEvent extends PaymentEvent {

	private final DomainEventPublisher<PaymentCompletedEvent> publisher;

	public PaymentCompletedEvent(Payment payment, ZonedDateTime createdAt,
			DomainEventPublisher<PaymentCompletedEvent> publisher) {
		super(payment, createdAt, Collections.emptyList());
		this.publisher = publisher;
	}

	@Override
	public void fire() {
		publisher.publish(this);
	}

}
