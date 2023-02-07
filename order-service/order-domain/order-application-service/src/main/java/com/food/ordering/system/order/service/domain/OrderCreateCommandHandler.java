package com.food.ordering.system.order.service.domain;

import javax.validation.Valid;

import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderCreateCommandHandler {

	private OrderCreateHelper orderCreateHelper;

	private OrderDataMapper orderDataMapper;

	private final OrderCreatedPaymentRequestMessagePublisher orderCreatedPaymentRequestMessagePublisher;

	public OrderCreateCommandHandler(OrderCreateHelper orderCreateHelper, OrderDataMapper orderDataMapper,
			OrderCreatedPaymentRequestMessagePublisher orderCreatedPaymentRequestMessagePublisher) {
		this.orderCreateHelper = orderCreateHelper;
		this.orderDataMapper = orderDataMapper;
		this.orderCreatedPaymentRequestMessagePublisher = orderCreatedPaymentRequestMessagePublisher;
	}

	public CreateOrderResponse createOrder(@Valid CreateOrderCommand createOrderCommand) {
		// The @Transaction annotaion should be called form another bean to works
		// That's why we refactor the transaction process into
		// OrderCreateHelper.persistOrder method
		// and call it in this bean to make it works
		// * This limitation is only for AOP Proxy,
		// * if we use AspectJ library, there's no such limitation. But AspectJ requires
		// * additional configuration
		OrderCreatedEvent orderCreatedEvent = orderCreateHelper.persistOrder(createOrderCommand);
		log.info("Order created with id: {}", orderCreatedEvent.getOrder().getId().getValue());
		orderCreatedPaymentRequestMessagePublisher.publish(orderCreatedEvent);

		// OrderEvent orderEvent = orderCreateHelper.persistOrder(createOrderCommand);
		// log.info("Order created with id: {}",
		// orderEvent.getOrder().getId().getValue());
		// orderEvent.fire();
		// return orderDataMapper.orderToCreateOrderResponse(orderEvent.getOrder(),
		// "Order craeted successfully");

		return orderDataMapper.orderToCreateOrderResponse(orderCreatedEvent.getOrder(), "Order craeted successfully");
	}
}
