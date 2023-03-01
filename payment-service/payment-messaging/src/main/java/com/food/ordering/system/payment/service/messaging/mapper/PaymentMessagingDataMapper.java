package com.food.ordering.system.payment.service.messaging.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueObject.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;

@Component
public class PaymentMessagingDataMapper {

	public PaymentResponseAvroModel orderEventPayloadToPaymentResponseAvroModel(String sagaId,
			OrderEventPayload payload) {
		return PaymentResponseAvroModel.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setSagaId(sagaId)
				.setPaymentId(payload.getPaymentId())
				.setCustomerId(payload.getCustomerId())
				.setOrderId(payload.getOrderId())
				.setPrice(payload.getPrice())
				.setCreatedAt(payload.getCreatedAt().toInstant())
				.setPaymentStatus(PaymentStatus.valueOf(payload.getPaymentStatus()))
				.setFailureMessages(payload.getFailureMessages())
				.build();
	}

	public PaymentRequest paymentRequestAvroModelToPaymentRequest(PaymentRequestAvroModel paymentRequestAvroModel) {
		return PaymentRequest.builder()
				.id(paymentRequestAvroModel.getId())
				.sagaId(paymentRequestAvroModel.getSagaId())
				.customerId(paymentRequestAvroModel.getCustomerId())
				.orderId(paymentRequestAvroModel.getOrderId())
				.price(paymentRequestAvroModel.getPrice())
				.createdAt(paymentRequestAvroModel.getCreatedAt())
				.paymentOrderStatus(PaymentOrderStatus.valueOf(paymentRequestAvroModel.getPaymentOrderStatus().name()))
				.build();
	}

}
