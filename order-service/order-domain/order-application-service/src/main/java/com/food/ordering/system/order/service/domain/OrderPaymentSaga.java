package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.OrderPaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.saga.SagaStep;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderPaymentSaga implements SagaStep<PaymentResponse, OrderPaidEvent, EmptyEvent> {

	private final OrderDomainService orderDomainService;
	private final OrderSagaHelper orderSagaHelper;
	private final OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher;

	public OrderPaymentSaga(OrderDomainService orderDomainService,
			OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher,
			OrderSagaHelper orderSagaHelper) {
		this.orderDomainService = orderDomainService;
		this.orderPaidRestaurantRequestMessagePublisher = orderPaidRestaurantRequestMessagePublisher;
		this.orderSagaHelper = orderSagaHelper;
	}

	@Override
	@Transactional
	public OrderPaidEvent process(PaymentResponse data) {
		log.info("Completing payment for order id: {}", data.getOrderId());

		Order order = orderSagaHelper.findOrder(data.getOrderId());
		OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order, orderPaidRestaurantRequestMessagePublisher);

		orderSagaHelper.saveOrder(order);

		log.info("Order with id: {} has been paid", order.getId().getValue());

		return orderPaidEvent;
	}

	@Override
	@Transactional
	public EmptyEvent rollback(PaymentResponse data) {
		log.info("Cancelling order id: {}", data.getOrderId());

		Order order = orderSagaHelper.findOrder(data.getOrderId());

		orderDomainService.cancelOrder(order, data.getFailureMessages());
		orderSagaHelper.saveOrder(order);

		log.info("Order id: {} has been cancelled", data.getOrderId());

		return EmptyEvent.INSTANCE;
	}

}
