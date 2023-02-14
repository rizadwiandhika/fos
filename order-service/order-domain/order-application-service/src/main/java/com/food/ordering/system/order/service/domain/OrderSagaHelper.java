package com.food.ordering.system.order.service.domain;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderSagaHelper {

	private final OrderRepository orderRepository;

	public OrderSagaHelper(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	public Order findOrder(String orderId) {
		Optional<Order> orOptional = orderRepository.findById(new OrderId(UUID.fromString(orderId)));
		if (orOptional.isEmpty()) {
			log.error("Order id: {} not found", orderId);
			throw new OrderNotFoundException("Order not found for id: " + orderId);
		}

		return orOptional.get();
	}

	public void saveOrder(Order order) {
		orderRepository.save(order);
	}

}
