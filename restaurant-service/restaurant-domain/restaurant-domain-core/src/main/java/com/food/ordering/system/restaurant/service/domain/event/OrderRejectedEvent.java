package com.food.ordering.system.restaurant.service.domain.event;

import java.time.ZonedDateTime;
import java.util.List;

import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;

public class OrderRejectedEvent extends OrderApprovalEvent {

	public OrderRejectedEvent(OrderApproval orderApproval, RestaurantId restaurantId, List<String> failureMessages,
			ZonedDateTime createdAt) {
		super(orderApproval, restaurantId, failureMessages, createdAt);
	}

}
