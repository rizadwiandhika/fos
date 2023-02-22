package com.food.ordering.system.order.service.domain;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated // enable @Valid annotation from interface
@Service
public class OrderApplicationServiceImpl implements OrderApplicationService {

	private final OrderTrackCommandHandler orderTrackCommandHandler;
	private final OrderCreateCommandHandler orderCreateCommandHandler;

	@Autowired
	public OrderApplicationServiceImpl(OrderTrackCommandHandler orderTrackCommandHandler,
			OrderCreateCommandHandler orderCreateCommandHandler) {
		this.orderTrackCommandHandler = orderTrackCommandHandler;
		this.orderCreateCommandHandler = orderCreateCommandHandler;

	}

	@Override
	public CreateOrderResponse createOrder(@Valid CreateOrderCommand createOrderCommand) {
		return orderCreateCommandHandler.createOrder(createOrderCommand);
	}

	@Override
	public TrackOrderResponse trackOrder(@Valid TrackOrderQuery trackOrderQuery) {
		return orderTrackCommandHandler.trackOrder(trackOrderQuery);
	}

}
