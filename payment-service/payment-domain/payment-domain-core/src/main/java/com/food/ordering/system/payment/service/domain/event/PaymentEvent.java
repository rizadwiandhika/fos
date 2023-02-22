package com.food.ordering.system.payment.service.domain.event;

import java.time.ZonedDateTime;
import java.util.List;

import com.food.ordering.system.domain.event.DomainEvent;
import com.food.ordering.system.payment.service.domain.entity.Payment;

public abstract class PaymentEvent implements DomainEvent<Payment> {

	private final Payment payment;
	private final ZonedDateTime createdAt;
	private final List<String> failureMesages;

	public PaymentEvent(Payment payment, ZonedDateTime createdAt, List<String> failureMesages) {
		this.payment = payment;
		this.createdAt = createdAt;
		this.failureMesages = failureMesages;
	}

	public Payment getPayment() {
		return payment;
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public List<String> getFailureMesages() {
		return failureMesages;
	}

}
