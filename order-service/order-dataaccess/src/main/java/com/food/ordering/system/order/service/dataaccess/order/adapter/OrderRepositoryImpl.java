package com.food.ordering.system.order.service.dataaccess.order.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;
import com.food.ordering.system.order.service.dataaccess.order.mapper.OrderDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.order.repository.OrderJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueObject.TrackingId;

@Component
public class OrderRepositoryImpl implements OrderRepository {

	private final OrderJpaRepository orderJpaRepository;
	private final OrderDataAccessMapper orderDataAccessMapper;

	public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository, OrderDataAccessMapper orderDataAccessMapper) {
		this.orderJpaRepository = orderJpaRepository;
		this.orderDataAccessMapper = orderDataAccessMapper;
	}

	@Override
	public Optional<Order> findByTrackingId(TrackingId trackingId) {
		return orderJpaRepository.findByTrackingId(trackingId.getValue())
				.map(orderDataAccessMapper::orderEntityToOrder);
	}

	@Override
	public Order save(Order order) {
		OrderEntity result = orderJpaRepository.save(orderDataAccessMapper.orderToOrderEntity(order));
		return orderDataAccessMapper.orderEntityToOrder(result);
	}

}
