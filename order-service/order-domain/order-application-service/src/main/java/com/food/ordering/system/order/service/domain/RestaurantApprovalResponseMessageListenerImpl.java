package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantapproval.RestaurantApprovalResponseMessageListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@Service
public class RestaurantApprovalResponseMessageListenerImpl implements RestaurantApprovalResponseMessageListener {

	private static final CharSequence DELIMITER = ";";
	private final OrderApprovalSaga orderApprovalSaga;

	public RestaurantApprovalResponseMessageListenerImpl(OrderApprovalSaga orderApprovalSaga) {
		this.orderApprovalSaga = orderApprovalSaga;
	}

	@Override
	public void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse) {
		orderApprovalSaga.process(restaurantApprovalResponse);
		log.info("Order id: {} is approved", restaurantApprovalResponse.getOrderId());
	}

	@Override
	public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {
		OrderCancelledEvent domainEvent = orderApprovalSaga.rollback(restaurantApprovalResponse);
		log.info("Order id: {} is rejected. Reasons: {}", restaurantApprovalResponse.getOrderId(),
				String.join(DELIMITER, restaurantApprovalResponse.getFailureMessages()));

		domainEvent.fire();
	}

}
