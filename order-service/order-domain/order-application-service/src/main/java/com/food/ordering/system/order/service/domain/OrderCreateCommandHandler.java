package com.food.ordering.system.order.service.domain;

import java.util.UUID;

import javax.validation.Valid;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderCreateCommandHandler {

	private final OrderCreateHelper orderCreateHelper;
	private final OrderDataMapper orderDataMapper;
	private final PaymentOutboxHelper paymentOutboxHelper;
	private final OrderSagaHelper orderSagaHelper;

	//// private final OrderCreatedPaymentRequestMessagePublisher
	//// orderCreatedPaymentRequestMessagePublisher;

	public OrderCreateCommandHandler(OrderCreateHelper orderCreateHelper, OrderDataMapper orderDataMapper,
			PaymentOutboxHelper paymentOutboxHelper, OrderSagaHelper orderSagaHelper) {
		this.orderCreateHelper = orderCreateHelper;
		this.orderDataMapper = orderDataMapper;
		this.paymentOutboxHelper = paymentOutboxHelper;
		this.orderSagaHelper = orderSagaHelper;
	}

	@Transactional
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
		CreateOrderResponse createOrderResponse = orderDataMapper
				.orderToCreateOrderResponse(orderCreatedEvent.getOrder(), "Order created successfully");

		paymentOutboxHelper.savePaymentOutboxMessage(
				orderDataMapper.orderCreatedEventToOrderPaymentEventPayload(orderCreatedEvent),
				orderCreatedEvent.getOrder().getOrderStatus(),
				orderSagaHelper.orderStatusToSagaStatus(orderCreatedEvent.getOrder().getOrderStatus()),
				OutboxStatus.STARTED,
				UUID.randomUUID());

		log.info("Order created with id: {}", orderCreatedEvent.getOrder().getId().getValue());

		return createOrderResponse;

		//// orderCreatedPaymentRequestMessagePublisher.publish(orderCreatedEvent);
		//// return
		//// orderDataMapper.orderToCreateOrderResponse(orderCreatedEvent.getOrder(),
		//// "Order craeted successfully");

		// OrderEvent orderEvent = orderCreateHelper.persistOrder(createOrderCommand);
		// log.info("Order created with id: {}",
		// orderEvent.getOrder().getId().getValue());
		// orderEvent.fire();
		// return orderDataMapper.orderToCreateOrderResponse(orderEvent.getOrder(),
		// "Order craeted successfully");

	}
}
