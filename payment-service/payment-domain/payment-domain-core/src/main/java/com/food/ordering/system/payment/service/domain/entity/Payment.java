package com.food.ordering.system.payment.service.domain.entity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueObject.CustomerId;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.PaymentStatus;
import com.food.ordering.system.payment.service.domain.valueobject.PaymentId;

public class Payment extends AggregateRoot<PaymentId> {

	private final OrderId orderId;
	private final CustomerId customerId;
	private final Money price;

	private PaymentStatus paymentStatus;
	private ZonedDateTime createdAt;

	private Payment(Builder builder) {
		setId(builder.paymentId);
		this.orderId = builder.orderId;
		this.customerId = builder.customerId;
		this.price = builder.price;
		this.paymentStatus = builder.paymentStatus;
		this.createdAt = builder.createdAt;
	}

	// Core business logic methods
	public void initializePayment() {
		setId(new PaymentId(UUID.randomUUID()));
		createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
	}

	public void validatePayment(List<String> failureMessages) {
		if (price == null || !price.isGreaterThanZero()) {
			failureMessages.add("Total price must be greater than zero");
		}
	}

	public void updateStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public CustomerId getCustomerId() {
		return customerId;
	}

	public OrderId getOrderId() {
		return orderId;
	}

	public Money getPrice() {
		return price;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	// Builder pattern for this class
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private PaymentId paymentId;
		private OrderId orderId;
		private CustomerId customerId;
		private Money price;
		private PaymentStatus paymentStatus;
		private ZonedDateTime createdAt;

		private Builder() {
		}

		public Builder withPaymentId(PaymentId paymentId) {
			this.paymentId = paymentId;
			return this;
		}

		public Builder withOrderId(OrderId orderId) {
			this.orderId = orderId;
			return this;
		}

		public Builder withCustomerId(CustomerId customerId) {
			this.customerId = customerId;
			return this;
		}

		public Builder withPrice(Money price) {
			this.price = price;
			return this;
		}

		public Builder withPaymentStatus(PaymentStatus paymentStatus) {
			this.paymentStatus = paymentStatus;
			return this;
		}

		public Builder withCreatedAt(ZonedDateTime createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Payment build() {
			return new Payment(this);
		}
	}

}