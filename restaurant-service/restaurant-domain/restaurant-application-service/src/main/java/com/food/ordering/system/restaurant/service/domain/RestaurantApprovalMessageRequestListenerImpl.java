package com.food.ordering.system.restaurant.service.domain;

import org.springframework.stereotype.Service;

import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.ports.input.message.listener.RestaurantApprovalMessageRequestListener;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RestaurantApprovalMessageRequestListenerImpl implements RestaurantApprovalMessageRequestListener {

	private final RestaurantApprovalRequestHelper restaurantApprovalRequestHelper;

	public RestaurantApprovalMessageRequestListenerImpl(
			RestaurantApprovalRequestHelper restaurantApprovalRequestHelper) {
		this.restaurantApprovalRequestHelper = restaurantApprovalRequestHelper;
	}

	@Override
	public void approveOrder(RestaurantApprovalRequest restaurantApprovalRequest) {
		OrderApprovalEvent orderApprovalEvent = restaurantApprovalRequestHelper
				.persistOrderApproval(restaurantApprovalRequest);

		orderApprovalEvent.fire();
	}

}
