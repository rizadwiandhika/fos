package com.food.ordering.system.order.service.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderDomainServiceImpl implements OrderDomainService {

	private final String UTC = "UTC";

	@Override
	public void approveOrder(Order order) {
		order.approve();

		log.info("Order approved: {}", order);

	}

	@Override
	public void cancelOrder(Order order, List<String> reasons) {
		order.cancel(reasons);

		log.info("Order is cancelled for order id: {}", order.getId().getValue());
	}

	@Override
	public OrderCancelledEvent cancelOrderPayment(Order order, List<String> reasons) {
		order.initCancel(reasons);

		log.info("Cancelling ordder for order id: {}", order.getId().getValue());

		return new OrderCancelledEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
	}

	@Override
	public OrderPaidEvent payOrder(Order order) {
		order.pay();

		log.info("Order {} is paid", order.getId());

		return new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
	}

	@Override
	public OrderCreatedEvent validateAndInitiateOrder(Order order, Restaurant restaurant) {
		validateRestaurant(restaurant);
		setOrderProductInformation(order, restaurant);

		order.validateOrder();
		order.initializeOrder();

		log.info("Order with id {} is created", order.getId().getValue());

		return new OrderCreatedEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
	}

	private void validateRestaurant(Restaurant restaurant) {
		if (restaurant.isActive() == false) {
			throw new OrderDomainException("Restaurant is not active");
		}
	}

	private void setOrderProductInformation(Order order, Restaurant restaurant) {
		order.getOrderItems().forEach((orderItem) -> restaurant.getProducts().forEach((restaurantProduct) -> {
			Product itemProduct = orderItem.getProduct();
			if (itemProduct.equals(restaurantProduct)) {
				itemProduct.updateWithConfirmedNameAndPrice(restaurantProduct.getName(),
						restaurantProduct.getPrice());
			}
		}));
	}

}
