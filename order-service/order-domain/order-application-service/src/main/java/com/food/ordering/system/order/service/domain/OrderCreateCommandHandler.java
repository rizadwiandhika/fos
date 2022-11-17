package com.food.ordering.system.order.service.domain;

import javax.validation.Valid;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
		// * if we use AspectJ library, there is no suchlimitation. But AspectJ requires
		// * additional configuration
		OrderCreatedEvent orderCreatedEvent = orderCreateHelper.persistOrder(createOrderCommand);
		log.info("Order created with id: {}", orderCreatedEvent.getOrder().getId().getValue());

		orderCreatedPaymentRequestMessagePublisher.publish(orderCreatedEvent);

		return orderDataMapper.orderToCreateOrderResponse(orderCreatedEvent.getOrder(), "Order craeted successfully");
	}
}
