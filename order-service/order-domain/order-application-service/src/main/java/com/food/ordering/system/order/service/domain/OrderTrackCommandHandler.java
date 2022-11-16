package com.food.ordering.system.order.service.domain;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueObject.TrackingId;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderTrackCommandHandler {

	private final OrderDataMapper orderDataMapper;

	private final OrderRepository orderRepository;

	public OrderTrackCommandHandler(OrderDataMapper orderDataMapper, OrderRepository orderRepository) {
		this.orderDataMapper = orderDataMapper;
		this.orderRepository = orderRepository;
	}

	@Transactional(readOnly = true)
	public TrackOrderResponse trackOrder(@Valid TrackOrderQuery trackOrderQuery) {
		Optional<Order> optionalOrder = orderRepository
				.findByTrackingId(new TrackingId(trackOrderQuery.getOrderTrackingId()));

		if (optionalOrder.isEmpty()) {
			log.warn("Order not found!");
			throw new OrderNotFoundException("Order not found for id: " + trackOrderQuery.getOrderTrackingId());
		}

		Order order = optionalOrder.get();
		log.info("Tracking order id: {}", order.getId().getValue());

		return orderDataMapper.orderToTrackOrderResponse(order);
	}
}
