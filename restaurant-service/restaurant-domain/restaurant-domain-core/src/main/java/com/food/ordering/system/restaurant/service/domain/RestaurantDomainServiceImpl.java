package com.food.ordering.system.restaurant.service.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import com.food.ordering.system.domain.DomainConstants;
import com.food.ordering.system.domain.valueObject.OrderApprovalStatus;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestaurantDomainServiceImpl implements RestaurantDomainService {

	@Override
	public OrderApprovalEvent validate(Restaurant restaurant, List<String> failureMessages) {

		log.info("Validating order: {}", restaurant.getOrderDetail().getId().getValue());
		restaurant.validateOrder(failureMessages);

		if (failureMessages.isEmpty()) {
			restaurant.constructOrderApproal(OrderApprovalStatus.APPROVED);
			log.info("Order is approved for order: {}", restaurant.getOrderDetail().getId().getValue().toString());
			return new OrderApprovedEvent(restaurant.getOrderApproval(), restaurant.getId(), failureMessages,
					ZonedDateTime.now(ZoneId.of(DomainConstants.UTC)));
		}

		restaurant.constructOrderApproal(OrderApprovalStatus.REJECTED);
		log.info("Order is rejected for order: {}", restaurant.getOrderDetail().getId().getValue());
		return new OrderRejectedEvent(restaurant.getOrderApproval(), restaurant.getId(), failureMessages,
				ZonedDateTime.now(ZoneId.of(DomainConstants.UTC)));
	}

}
