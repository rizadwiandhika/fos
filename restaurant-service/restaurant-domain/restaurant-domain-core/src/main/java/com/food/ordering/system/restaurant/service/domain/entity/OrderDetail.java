package com.food.ordering.system.restaurant.service.domain.entity;

import java.util.List;

import com.food.ordering.system.domain.entity.BaseEntity;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.OrderStatus;

public class OrderDetail extends BaseEntity<OrderId> {

	private OrderStatus orderStatus;
	private Money totalAmount;
	private final List<Product> products;

	private OrderDetail(Builder builder) {
		setId(builder.orderId);
		this.orderStatus = builder.orderStatus;
		this.totalAmount = builder.totalAmount;
		this.products = builder.products;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public Money getTotalAmount() {
		return totalAmount;
	}

	public List<Product> getProducts() {
		return products;
	}

	public static Builder builder() {
		return new Builder();
	}

	// Create a builder for this class
	public static class Builder {
		private OrderId orderId;
		private OrderStatus orderStatus;
		private Money totalAmount;
		private List<Product> products;

		public Builder setOrderId(OrderId orderId) {
			this.orderId = orderId;
			return this;
		}

		public Builder setOrderStatus(OrderStatus orderStatus) {
			this.orderStatus = orderStatus;
			return this;
		}

		public Builder setTotalAmount(Money totalAmount) {
			this.totalAmount = totalAmount;
			return this;
		}

		public Builder setProducts(List<Product> products) {
			this.products = products;
			return this;
		}

		public OrderDetail build() {
			return new OrderDetail(this);
		}
	}
}
