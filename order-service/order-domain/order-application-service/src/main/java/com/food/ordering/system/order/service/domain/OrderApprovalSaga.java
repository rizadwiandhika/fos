package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import com.food.ordering.system.saga.SagaStep;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse, EmptyEvent, OrderCancelledEvent> {

	private final OrderSagaHelper orderSagaHelper;
	private final OrderDomainService orderDomainService;
	private final OrderCancelledPaymentRequestMessagePublisher orderCancelledPaymentRequestMessagePublisher;

	public OrderApprovalSaga(OrderSagaHelper orderSagaHelper, OrderDomainService orderDomainService,
			OrderCancelledPaymentRequestMessagePublisher orderCancelledPaymentRequestMessagePublisher) {
		this.orderSagaHelper = orderSagaHelper;
		this.orderDomainService = orderDomainService;
		this.orderCancelledPaymentRequestMessagePublisher = orderCancelledPaymentRequestMessagePublisher;
	}

	@Override
	@Transactional
	public EmptyEvent process(RestaurantApprovalResponse data) {
		Order order = orderSagaHelper.findOrder(data.getOrderId());

		orderDomainService.approveOrder(order);
		orderSagaHelper.saveOrder(order);

		return EmptyEvent.INSTANCE;
	}

	@Override
	@Transactional
	public OrderCancelledEvent rollback(RestaurantApprovalResponse data) {
		log.info("Cancelling order for order id: {}", data.getOrderId());

		Order order = orderSagaHelper.findOrder(data.getOrderId());
		OrderCancelledEvent domainEvent = orderDomainService.cancelOrderPayment(order, data.getFailureMessages(),
				orderCancelledPaymentRequestMessagePublisher);

		orderSagaHelper.saveOrder(order);
		log.info("order id: {} is in cancelling status", data.getOrderId());

		return domainEvent;
	}

}
