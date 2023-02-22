package com.food.ordering.system.order.service.domain.event;

import java.time.ZonedDateTime;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.entity.Order;

public class OrderPaidEvent extends OrderEvent {

	private final DomainEventPublisher<OrderPaidEvent> publisher;

	public OrderPaidEvent(Order order, ZonedDateTime createdAt, DomainEventPublisher<OrderPaidEvent> publisher) {
		super(order, createdAt);
		this.publisher = publisher;
	}

	@Override
	public void fire() {
		publisher.publish(this);
	}

}
