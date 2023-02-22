package com.food.ordering.system.payment.service.domain.mapper;

import static com.food.ordering.system.domain.DomainConstants.UTC;
import static com.food.ordering.system.outbox.OutboxStatus.STARTED;
import static com.food.ordering.system.saga.order.SagaConstant.ORDER_SAGA_NAME;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentDomainException;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentDataMapper {

	private ObjectMapper objectMapper;

	public PaymentDataMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public Payment paymentRequestToPayment(PaymentRequest paymentRequest) {
		return Payment.builder()
				.withOrderId(new OrderId(UUID.fromString(paymentRequest.getOrderId())))
				.withCustomerId(new CustomerId(UUID.fromString(paymentRequest.getCustomerId())))
				.withPrice(new Money(paymentRequest.getPrice()))
				.build();
	}

	public OrderEventPayload paymentEventToOrderEventPayload(PaymentEvent domainEvent) {
		return OrderEventPayload.builder()
				.paymentId(domainEvent.getPayment().getId().getValue().toString())
				.customerId(domainEvent.getPayment().getCustomerId().getValue().toString())
				.orderId(domainEvent.getPayment().getOrderId().getValue().toString())
				.price(domainEvent.getPayment().getPrice().getAmount())
				.createdAt(domainEvent.getCreatedAt())
				.paymentStatus(domainEvent.getPayment().getPaymentStatus().name())
				.failureMessages(domainEvent.getFailureMesages())
				.build();
	}

	// public OrderOutboxMessage paymentEventToOrderOutboxMessage(UUID sagaId,
	// PaymentEvent domainEvent) {
	// return OrderOutboxMessage.builder()
	// .id(UUID.randomUUID())
	// .sagaId(sagaId)
	// .createdAt(domainEvent.getCreatedAt())
	// .processedAt(ZonedDateTime.now(ZoneId.of(UTC)))
	// .type(ORDER_SAGA_NAME)
	// .payload(getPayload(paymentEventToOrderEventPayload(domainEvent)))
	// .paymentStatus(domainEvent.getPayment().getPaymentStatus())
	// .outboxStatus(STARTED)
	// .build();
	// }

	private String getPayload(OrderEventPayload payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (Exception e) {
			log.info("Unable to convert OrderEventPayload");
			throw new PaymentDomainException("Unable to convert OrderEventPayload");
		}
	}

}
