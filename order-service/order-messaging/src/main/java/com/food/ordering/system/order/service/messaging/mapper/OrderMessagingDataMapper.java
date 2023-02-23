package com.food.ordering.system.order.service.messaging.mapper;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueObject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.kafka.order.avro.model.CustomerAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.Product;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantOrderStatus;
import com.food.ordering.system.order.service.domain.dto.message.CustomerMessage;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;

@Component
public class OrderMessagingDataMapper {

	public PaymentResponse paymentResponseAvroModelToPaymentResponse(
			PaymentResponseAvroModel paymentResponseAvroModel) {
		return PaymentResponse.builder()
				.id(paymentResponseAvroModel.getId())
				.sagaId(paymentResponseAvroModel.getSagaId())
				.paymentId(paymentResponseAvroModel.getPaymentId())
				.customerId(paymentResponseAvroModel.getCustomerId())
				.orderId(paymentResponseAvroModel.getOrderId())
				.price(paymentResponseAvroModel.getPrice())
				.createdAt(paymentResponseAvroModel.getCreatedAt())
				.paymentStatus(PaymentStatus.valueOf(paymentResponseAvroModel.getPaymentStatus().name()))
				.failureMessages(paymentResponseAvroModel.getFailureMessages())
				.build();
	}

	public RestaurantApprovalResponse approvalResponseAvroModelToRestaurantApprovalResponse(
			RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel) {
		return RestaurantApprovalResponse.builder()
				.id(restaurantApprovalResponseAvroModel.getId())
				.sagaId(restaurantApprovalResponseAvroModel.getSagaId())
				.restaurantId(restaurantApprovalResponseAvroModel.getRestaurantId())
				.orderId(restaurantApprovalResponseAvroModel.getOrderId())
				.createdAt(restaurantApprovalResponseAvroModel.getCreatedAt())
				.orderApprovalStatus(OrderApprovalStatus
						.valueOf(restaurantApprovalResponseAvroModel.getOrderApprovalStatus().name()))
				.failureMessages(restaurantApprovalResponseAvroModel.getFailureMessages())
				.build();
	}

	public PaymentRequestAvroModel orderPaymentEventPayloadToPaymentRequestAvroModel(String sagaId,
			OrderPaymentEventPayload payload) {
		return PaymentRequestAvroModel.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setSagaId(sagaId)
				.setCustomerId(payload.getCustomerId())
				.setOrderId(payload.getOrderId())
				.setPrice(payload.getPrice())
				.setCreatedAt(payload.getCreatedAt().toInstant())
				.setPaymentOrderStatus(PaymentOrderStatus.valueOf(payload.getPaymentOrderStatus()))
				.build();
	}

	public RestaurantApprovalRequestAvroModel orderApprovalEventPayloadToRestaurantApprovalRequestAvroModel(
			String sagaId, OrderApprovalEventPayload payload) {
		return RestaurantApprovalRequestAvroModel.newBuilder()
				.setId(UUID.randomUUID().toString())
				.setSagaId(sagaId)
				.setRestaurantId(payload.getRestaurantId())
				.setOrderId(payload.getOrderId())
				.setRestaurantOrderStatus(RestaurantOrderStatus.valueOf(payload.getRestaurantOrderStatus()))
				.setProducts(payload.getOrderApprovalEventProducts().stream().map((p -> Product.newBuilder()
						.setId(p.getId())
						.setQuantity(p.getQuantity())
						.build())).collect(Collectors.toList()))
				.setPrice(payload.getPrice())
				.setCreatedAt(payload.getCreatedAt().toInstant())
				.build();
	}

	public CustomerMessage customerAvroModelToCustomerMessage(CustomerAvroModel message) {
		return CustomerMessage.builder()
				.id(UUID.fromString(message.getId()))
				.username(message.getUsername())
				.firstName(message.getFirstName())
				.lastName(message.getLastName())
				.build();
	}

}
