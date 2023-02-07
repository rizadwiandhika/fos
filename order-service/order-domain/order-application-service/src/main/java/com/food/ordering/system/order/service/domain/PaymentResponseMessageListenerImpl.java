package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@Service
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {

	private OrderDomainService orderDomainService;
	private OrderDataMapper orderDataMapper;
	private OrderCancelledPaymentRequestMessagePublisher orderCancelledPaymentRequestMessagePublisher;

	public PaymentResponseMessageListenerImpl(OrderDomainService orderDomainService, OrderDataMapper orderDataMapper,
			OrderCancelledPaymentRequestMessagePublisher orderCancelledPaymentRequestMessagePublisher) {
		this.orderDomainService = orderDomainService;
		this.orderDataMapper = orderDataMapper;
		this.orderCancelledPaymentRequestMessagePublisher = orderCancelledPaymentRequestMessagePublisher;
	}

	@Override
	public void paymentCancelled(PaymentResponse paymentResponse) {
		orderDomainService.cancelOrderPayment(null, null, orderCancelledPaymentRequestMessagePublisher);
	}

	@Override
	public void paymentCompleted(PaymentResponse paymentResponse) {
		// TODO Auto-generated method stub

	}

}
