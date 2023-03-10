package com.food.ordering.system.payment.service.domain.event;

import java.time.ZonedDateTime;
import java.util.List;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.payment.service.domain.entity.Payment;

public class PaymentFailedEvent extends PaymentEvent {

	public PaymentFailedEvent(Payment payment, ZonedDateTime createdAt, List<String> failureMesages) {
		super(payment, createdAt, failureMesages);
	}

}
