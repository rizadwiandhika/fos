package com.food.ordering.system.order.service.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderPaymentSaga implements SagaStep<PaymentResponse> {

	private final OrderDomainService orderDomainService;
	private final OrderSagaHelper orderSagaHelper;
	private final PaymentOutboxHelper paymentOutboxHelper;
	private final ApprovalOutboxHelper approvalOutboxHelper;
	private final OrderDataMapper orderDataMapper;

	public OrderPaymentSaga(OrderDomainService orderDomainService, OrderSagaHelper orderSagaHelper,
			PaymentOutboxHelper paymentOutboxHelper, ApprovalOutboxHelper approvalOutboxHelper,
			OrderDataMapper orderDataMapper) {
		this.orderDomainService = orderDomainService;
		this.orderSagaHelper = orderSagaHelper;
		this.paymentOutboxHelper = paymentOutboxHelper;
		this.approvalOutboxHelper = approvalOutboxHelper;
		this.orderDataMapper = orderDataMapper;
	}

	// For more= information about synchronization or locking,
	// https://www.udemy.com/course/microservices-clean-architecture-ddd-saga-outbox-kafka-kubernetes/learn/lecture/32243828#questions/18050330
	@Override
	@Transactional
	public void process(PaymentResponse paymentResponse) {
		Optional<OrderPaymentOutboxMessage> result = paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
				UUID.fromString(paymentResponse.getSagaId()),
				SagaStatus.STARTED);

		if (result.isEmpty()) {
			log.info("Outbox with saga id: {} is already proccessed", paymentResponse.getSagaId());
			return;
		}

		OrderPaymentOutboxMessage paymentOutbox = result.get();
		OrderPaidEvent domainEvent = completePayment(paymentResponse);
		SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(domainEvent.getOrder().getOrderStatus());

		// Optimistic locking happens here
		paymentOutboxHelper
				.save(getUpdatedPaymentOutboxMessage(paymentOutbox, domainEvent.getOrder().getOrderStatus(),
						sagaStatus));

		approvalOutboxHelper.saveApprovalOutboxMessage(
				orderDataMapper.orderPaidEventToOrderApprovalEventPayload(domainEvent),
				domainEvent.getOrder().getOrderStatus(),
				sagaStatus,
				OutboxStatus.STARTED,
				UUID.fromString(paymentResponse.getSagaId()));

		log.info("Order with id: {} has been paid", domainEvent.getOrder().getId().getValue());
	}

	@Override
	@Transactional
	public void rollback(PaymentResponse paymentResponse) {
		Optional<OrderPaymentOutboxMessage> result = paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
				UUID.fromString(paymentResponse.getSagaId()),
				getCurrentSagaStatus(paymentResponse.getPaymentStatus()));

		if (result.isEmpty()) {
			log.info("Rollback outbox with saga id: {} is already proccessed", paymentResponse.getSagaId());
			return;
		}

		OrderPaymentOutboxMessage paymentOutbox = result.get();
		Order order = rollbackOrderForPayment(paymentResponse);
		SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());

		paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(paymentOutbox, order.getOrderStatus(), sagaStatus));
		if (paymentResponse.getPaymentStatus() == PaymentStatus.CANCELLED) {
			approvalOutboxHelper
					.save(getUpdatedApprovalOutboxMessage(paymentOutbox.getSagaId(), order.getOrderStatus(),
							sagaStatus));
		}

		log.info("Order id: {} has been cancelled", paymentResponse.getOrderId());
	}

	private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(UUID sagaId,
			OrderStatus orderStatus, SagaStatus sagaStatus) {
		Optional<OrderApprovalOutboxMessage> result = approvalOutboxHelper
				.getOrderApprovalOutboxBySagaIdAndSagaStatus(sagaId, SagaStatus.COMPENSATING);
		if (result.isEmpty()) {
			throw new OrderDomainException("Approval outbox with saga id: " + sagaId + " is already proccessed");
		}

		OrderApprovalOutboxMessage approvalOutbox = result.get();
		approvalOutbox.setOrderStatus(orderStatus);
		approvalOutbox.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
		approvalOutbox.setSagaStatus(sagaStatus);

		return approvalOutbox;
	}

	private Order rollbackOrderForPayment(PaymentResponse paymentResponse) {
		log.info("Cancelling order id: {}", paymentResponse.getOrderId());
		Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
		orderDomainService.cancelOrder(order, paymentResponse.getFailureMessages());
		orderSagaHelper.saveOrder(order);

		return order;
	}

	private SagaStatus[] getCurrentSagaStatus(PaymentStatus paymentStatus) {
		return switch (paymentStatus) {
			case COMPLETED -> new SagaStatus[] { SagaStatus.STARTED };
			case CANCELLED -> new SagaStatus[] { SagaStatus.PROCESSING };
			case FAILED -> new SagaStatus[] { SagaStatus.STARTED, SagaStatus.PROCESSING };
		};
	}

	private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(
			OrderPaymentOutboxMessage orderPaymentOutboxMessage,
			OrderStatus orderStatus, SagaStatus sagaStatus) {
		orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
		orderPaymentOutboxMessage.setOrderStatus(orderStatus);
		orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
		return orderPaymentOutboxMessage;
	}

	private OrderPaidEvent completePayment(PaymentResponse paymentResponse) {
		log.info("Completing payment for order id: {}", paymentResponse.getOrderId());
		Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
		OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order);
		orderSagaHelper.saveOrder(order);

		return orderPaidEvent;
	}
}
