package com.food.ordering.system.restaurant.service.domain.entity;

import java.util.List;
import java.util.UUID;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueObject.OrderStatus;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.valueobject.OrderApprovalId;

public class Restaurant extends AggregateRoot<RestaurantId> {
	private OrderApproval orderApproval;
	private boolean active;
	private final OrderDetail orderDetail;

	public void validateOrder(List<String> failureMessages) {
		if (orderDetail.getOrderStatus() != OrderStatus.PAID) {
			failureMessages.add("Payment is not completed for order: " + orderDetail.getId());
		}

		Money totalAmount = orderDetail.getProducts().stream().map(product -> {
			if (!product.isAvailable()) {
				failureMessages.add("Product: " + product.getId() + " is not available");
			}
			return product.getPrice().multiply(product.getQuantity());
		}).reduce(Money.ZERO, Money::add);

		if (!totalAmount.equals(totalAmount)) {
			failureMessages.add("Total price is not corrent for order: " + orderDetail.getId());
		}
	}

	public void constructOrderApproal(OrderApprovalStatus orderApprovalStatus) {
		this.orderApproval = OrderApproval.builder()
				.setOrderApprovalId(new OrderApprovalId(UUID.randomUUID()))
				.setOrderApprovalStatus(orderApprovalStatus)
				.setRestaurantId(this.getId())
				.setOrderId(this.orderDetail.getId())
				.build();
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	// constructor
	private Restaurant(Builder builder) {
		super.setId(builder.restaurantId);
		this.orderApproval = builder.orderApproval;
		this.active = builder.active;
		this.orderDetail = builder.orderDetail;
	}

	public OrderApproval getOrderApproval() {
		return orderApproval;
	}

	public boolean isActive() {
		return active;
	}

	public OrderDetail getOrderDetail() {
		return orderDetail;
	}

	public static Builder builder() {
		return new Builder();
	}

	// Create a builder for this class
	public static class Builder {
		private RestaurantId restaurantId;
		private OrderApproval orderApproval;
		private boolean active;
		private OrderDetail orderDetail;

		public Builder setRestaurantId(RestaurantId restaurantId) {
			this.restaurantId = restaurantId;
			return this;
		}

		public Builder setOrderApproval(OrderApproval orderApproval) {
			this.orderApproval = orderApproval;
			return this;
		}

		public Builder setActive(boolean active) {
			this.active = active;
			return this;
		}

		public Builder setOrderDetail(OrderDetail orderDetail) {
			this.orderDetail = orderDetail;
			return this;
		}

		public Restaurant build() {
			return new Restaurant(this);
		}
	}

}
