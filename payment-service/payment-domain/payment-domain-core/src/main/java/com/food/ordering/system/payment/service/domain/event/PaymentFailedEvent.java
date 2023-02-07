package com.food.ordering.system.payment.service.domain.event;

import java.time.ZonedDateTime;
import java.util.List;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.payment.service.domain.entity.Payment;

public class PaymentFailedEvent extends PaymentEvent {

	private final DomainEventPublisher<PaymentFailedEvent> publisher;

	public PaymentFailedEvent(Payment payment, ZonedDateTime createdAt, List<String> failureMesages,
			DomainEventPublisher<PaymentFailedEvent> publisher) {
		super(payment, createdAt, failureMesages);
		this.publisher = publisher;
	}

	@Override
	public void fire() {
		publisher.publish(this);
	}

}
