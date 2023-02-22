package com.food.ordering.system.restaurant.service.domain.event;

import java.time.ZonedDateTime;
import java.util.List;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;

public class OrderApprovedEvent extends OrderApprovalEvent {

	private final DomainEventPublisher<OrderApprovedEvent> publisher;

	public OrderApprovedEvent(OrderApproval orderApproval, RestaurantId restaurantId, List<String> failureMessages,
			ZonedDateTime createdAt, DomainEventPublisher<OrderApprovedEvent> publisher) {
		super(orderApproval, restaurantId, failureMessages, createdAt);
		this.publisher = publisher;
	}

	@Override
	public void fire() {
		publisher.publish(this);
	}

}
