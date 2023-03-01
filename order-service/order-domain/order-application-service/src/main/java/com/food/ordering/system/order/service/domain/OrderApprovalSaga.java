package com.food.ordering.system.order.service.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse> {

	private final OrderDomainService orderDomainService;
	private final OrderRepository orderRepository;
	private final OrderSagaHelper orderSagaHelper;
	private final PaymentOutboxHelper paymentOutboxHelper;
	private final ApprovalOutboxHelper approvalOutboxHelper;
	private final OrderDataMapper orderDataMapper;

	public OrderApprovalSaga(OrderDomainService orderDomainService, OrderRepository orderRepository,
			OrderSagaHelper orderSagaHelper, PaymentOutboxHelper paymentOutboxHelper,
			ApprovalOutboxHelper approvalOutboxHelper, OrderDataMapper orderDataMapper) {
		this.orderDomainService = orderDomainService;
		this.orderRepository = orderRepository;
		this.orderSagaHelper = orderSagaHelper;
		this.paymentOutboxHelper = paymentOutboxHelper;
		this.approvalOutboxHelper = approvalOutboxHelper;
		this.orderDataMapper = orderDataMapper;
	}

	@Override
	@Transactional
	public void process(RestaurantApprovalResponse restaurantApprovalResponse) {
		Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse = approvalOutboxHelper
				.getOrderApprovalOutboxBySagaIdAndSagaStatus(
						UUID.fromString(restaurantApprovalResponse.getSagaId()), SagaStatus.PROCESSING);

		if (orderApprovalOutboxMessageResponse.isEmpty()) {
			log.info("Order approval outbox message not found for saga id: {} and saga status: {}",
					restaurantApprovalResponse.getSagaId(), SagaStatus.PROCESSING);
			return;
		}

		OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();
		Order order = approveOrder(restaurantApprovalResponse);
		SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());

		approvalOutboxHelper.save(
				getUpdatedOrderApprovalOutboxMessage(orderApprovalOutboxMessage, order.getOrderStatus(), sagaStatus));

		paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(restaurantApprovalResponse.getSagaId(),
				order.getOrderStatus(), sagaStatus));
	}

	@Override
	@Transactional
	public void rollback(RestaurantApprovalResponse restaurantApprovalResponse) {
		Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse = approvalOutboxHelper
				.getOrderApprovalOutboxBySagaIdAndSagaStatus(
						UUID.fromString(restaurantApprovalResponse.getSagaId()), SagaStatus.PROCESSING);

		if (orderApprovalOutboxMessageResponse.isEmpty()) {
			log.info("Order approval outbox message not found for saga id: {} and saga status: {}",
					restaurantApprovalResponse.getSagaId(), SagaStatus.PROCESSING);
			return;
		}

		OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();
		OrderCancelledEvent domainEvent = rollbackOrder(restaurantApprovalResponse);
		SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(domainEvent.getOrder().getOrderStatus());

		approvalOutboxHelper.save(
				getUpdatedOrderApprovalOutboxMessage(orderApprovalOutboxMessage,
						domainEvent.getOrder().getOrderStatus(), sagaStatus));

		paymentOutboxHelper.savePaymentOutboxMessage(
				orderDataMapper.orderCancelledEventToOrderPaymentEventPayload(domainEvent),
				domainEvent.getOrder().getOrderStatus(),
				sagaStatus,
				OutboxStatus.STARTED,
				UUID.fromString(restaurantApprovalResponse.getSagaId()));

	}

	private Order approveOrder(RestaurantApprovalResponse restaurantApprovalResponse) {
		Order order = orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
		orderDomainService.approveOrder(order);
		orderSagaHelper.saveOrder(order);
		return order;
	}

	private OrderCancelledEvent rollbackOrder(RestaurantApprovalResponse restaurantApprovalResponse) {
		Order order = orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
		OrderCancelledEvent domainEvent = orderDomainService.cancelOrderPayment(order,
				restaurantApprovalResponse.getFailureMessages());
		orderSagaHelper.saveOrder(order);

		log.info("order id: {} is in cancelling status", restaurantApprovalResponse.getOrderId());
		return domainEvent;
	}

	private OrderApprovalOutboxMessage getUpdatedOrderApprovalOutboxMessage(
			OrderApprovalOutboxMessage orderApprovalOutboxMessage, OrderStatus orderStatus, SagaStatus sagaStatus) {
		orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
		orderApprovalOutboxMessage.setOrderStatus(orderStatus);
		orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
		return orderApprovalOutboxMessage;
	}

	private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(String sagaId, OrderStatus orderStatus,
			SagaStatus sagaStatus) {
		Optional<OrderPaymentOutboxMessage> pOptional = paymentOutboxHelper
				.getPaymentOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(sagaId), SagaStatus.PROCESSING);

		if (pOptional.isEmpty()) {
			throw new OrderDomainException("Payment outbox message not found for saga id: " + sagaId
					+ " and saga status: " + SagaStatus.PROCESSING);
		}

		OrderPaymentOutboxMessage orderPaymentOutboxMessage = pOptional.get();

		orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
		orderPaymentOutboxMessage.setOrderStatus(orderStatus);
		orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));

		return orderPaymentOutboxMessage;
	}

}
