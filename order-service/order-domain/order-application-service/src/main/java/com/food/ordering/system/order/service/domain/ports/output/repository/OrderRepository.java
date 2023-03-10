package com.food.ordering.system.order.service.domain.ports.output.repository;

import java.util.Optional;

import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.valueObject.TrackingId;

public interface OrderRepository {

	Order save(Order order);

	Optional<Order> findByTrackingId(TrackingId trackingId);

	Optional<Order> findById(OrderId orderId);
}
