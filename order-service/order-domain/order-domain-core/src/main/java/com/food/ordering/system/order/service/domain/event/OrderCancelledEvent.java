package com.food.ordering.system.order.service.domain.event;

import java.time.ZonedDateTime;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.entity.Order;

public class OrderCancelledEvent extends OrderEvent {

	private final DomainEventPublisher<OrderCancelledEvent> publisher;

	public OrderCancelledEvent(Order order, ZonedDateTime createdAt,
			DomainEventPublisher<OrderCancelledEvent> publisher) {
		super(order, createdAt);
		this.publisher = publisher;
	}

	@Override
	public void fire() {
		publisher.publish(this);
	}

}
