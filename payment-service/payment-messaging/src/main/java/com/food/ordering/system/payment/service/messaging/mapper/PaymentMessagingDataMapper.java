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

@Component
public class PaymentMessagingDataMapper {

	public PaymentResponseAvroModel paymentCompletedEventToPaymentResponseAvroModel(
			PaymentCompletedEvent paymentCompletedEvent) {
		return PaymentResponseAvroModel.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setSagaId("")
				.setPaymentId(paymentCompletedEvent.getPayment().getId().getValue().toString())
				.setCustomerId(paymentCompletedEvent.getPayment().getCustomerId().getValue().toString())
				.setOrderId(paymentCompletedEvent.getPayment().getOrderId().getValue().toString())
				.setPrice(paymentCompletedEvent.getPayment().getPrice().getAmount())
				.setCreatedAt(paymentCompletedEvent.getPayment().getCreatedAt().toInstant())
				.setPaymentStatus(PaymentStatus.valueOf(paymentCompletedEvent.getPayment().getPaymentStatus().name()))
				.setFailureMessages(paymentCompletedEvent.getFailureMesages())
				.build();
	}

	public PaymentResponseAvroModel paymentCancelledEventToPaymentResponseAvroModel(
			PaymentCancelledEvent paymentCancelledEvent) {
		return PaymentResponseAvroModel.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setSagaId("")
				.setPaymentId(paymentCancelledEvent.getPayment().getId().getValue().toString())
				.setCustomerId(paymentCancelledEvent.getPayment().getCustomerId().getValue().toString())
				.setOrderId(paymentCancelledEvent.getPayment().getOrderId().getValue().toString())
				.setPrice(paymentCancelledEvent.getPayment().getPrice().getAmount())
				.setCreatedAt(paymentCancelledEvent.getPayment().getCreatedAt().toInstant())
				.setPaymentStatus(PaymentStatus.valueOf(paymentCancelledEvent.getPayment().getPaymentStatus().name()))
				.setFailureMessages(paymentCancelledEvent.getFailureMesages())
				.build();
	}

	public PaymentResponseAvroModel paymentFailedEventToPaymentResponseAvroModel(
			PaymentFailedEvent paymentFailedEvent) {
		return PaymentResponseAvroModel.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setSagaId("")
				.setPaymentId(paymentFailedEvent.getPayment().getId().getValue().toString())
				.setCustomerId(paymentFailedEvent.getPayment().getCustomerId().getValue().toString())
				.setOrderId(paymentFailedEvent.getPayment().getOrderId().getValue().toString())
				.setPrice(paymentFailedEvent.getPayment().getPrice().getAmount())
				.setCreatedAt(paymentFailedEvent.getPayment().getCreatedAt().toInstant())
				.setPaymentStatus(PaymentStatus.valueOf(paymentFailedEvent.getPayment().getPaymentStatus().name()))
				.setFailureMessages(paymentFailedEvent.getFailureMesages())
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
				.PaymentOrderStatus(PaymentOrderStatus.valueOf(paymentRequestAvroModel.getPaymentOrderStatus().name()))
				.build();
	}

}
