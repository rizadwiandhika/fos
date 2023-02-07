package com.food.ordering.system.order.service.domain;

import java.util.List;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;

public interface OrderDomainService {

	OrderCreatedEvent validateAndInitiateOrder(Order order, Restaurant restaurant,
			DomainEventPublisher<OrderCreatedEvent> publisher);

	OrderPaidEvent payOrder(Order order, DomainEventPublisher<OrderPaidEvent> publisher);

	void approveOrder(Order order);

	OrderCancelledEvent cancelOrderPayment(Order order, List<String> reasons,
			DomainEventPublisher<OrderCancelledEvent> publisher);

	void cancelOrder(Order order, List<String> reasons);
}
