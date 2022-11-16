package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.BaseEntity;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.order.service.domain.valueObject.OrderItemId;

public class OrderItem extends BaseEntity<OrderItemId> {
	private OrderId orderId;
	private final Product product;
	private final int quantity;
	private final Money price;
	private final Money subTotal; // price * quantity

	void initializeOrderItem(OrderId orderId, OrderItemId orderItemId) {
		this.orderId = orderId;
		super.setId(orderItemId);
	}

	boolean isPriceValid() {
		return price.isGreaterThanZero() && price.equals(product.getPrice()) && price.multiply(quantity).equals(subTotal);
	}

	private OrderItem(Builder builder) {
		super.setId(builder.orderItemId);
		this.product = builder.product;
		this.quantity = builder.quantity;
		this.price = builder.price;
		this.subTotal = builder.subTotal;
	}

	public static Builder builder() {
		return new Builder();
	}

	public OrderId getOrderId() {
		return orderId;
	}

	public Product getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}

	public Money getPrice() {
		return price;
	}

	public Money getSubTotal() {
		return subTotal;
	}

	public void setOrderId(OrderId orderId) {
		this.orderId = orderId;
	}

	// builder
	public static class Builder {
		private OrderItemId orderItemId;
		private Product product;
		private int quantity;
		private Money price;
		private Money subTotal;

		private Builder() {
		}

		public Builder setOrderItemId(OrderItemId orderItemId) {
			this.orderItemId = orderItemId;
			return this;
		}

		public Builder setProduct(Product product) {
			this.product = product;
			return this;
		}

		public Builder setQuantity(int quantity) {
			this.quantity = quantity;
			return this;
		}

		public Builder setPrice(Money price) {
			this.price = price;
			return this;
		}

		public Builder setSubTotal(Money subTotal) {
			this.subTotal = subTotal;
			return this;
		}

		public OrderItem build() {
			return new OrderItem(this);
		}
	}

}
