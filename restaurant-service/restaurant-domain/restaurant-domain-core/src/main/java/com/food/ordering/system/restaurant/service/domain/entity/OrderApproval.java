package com.food.ordering.system.restaurant.service.domain.entity;

import com.food.ordering.system.domain.entity.BaseEntity;
import com.food.ordering.system.domain.valueObject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.valueobject.OrderApprovalId;

public class OrderApproval extends BaseEntity<OrderApprovalId> {

	private OrderApproval(Builder builder) {
		super.setId(builder.orderApprovalId);
		this.restaurantId = builder.restaurantId;
		this.orderId = builder.orderId;
		this.orderApprovalStatus = builder.orderApprovalStatus;
	}

	private final RestaurantId restaurantId;
	private final OrderId orderId;
	private final OrderApprovalStatus orderApprovalStatus;

	public RestaurantId getRestaurantId() {
		return restaurantId;
	}

	public OrderId getOrderId() {
		return orderId;
	}

	public OrderApprovalStatus getOrderApprovalStatus() {
		return orderApprovalStatus;
	}

	public static Builder builder() {
		return new Builder();
	}

	// Create a builder for this class
	public static class Builder {
		private OrderApprovalId orderApprovalId;
		private RestaurantId restaurantId;
		private OrderId orderId;
		private OrderApprovalStatus orderApprovalStatus;

		public Builder setOrderApprovalId(OrderApprovalId orderApprovalId) {
			this.orderApprovalId = orderApprovalId;
			return this;
		}

		public Builder setRestaurantId(RestaurantId restaurantId) {
			this.restaurantId = restaurantId;
			return this;
		}

		public Builder setOrderId(OrderId orderId) {
			this.orderId = orderId;
			return this;
		}

		public Builder setOrderApprovalStatus(OrderApprovalStatus orderApprovalStatus) {
			this.orderApprovalStatus = orderApprovalStatus;
			return this;
		}

		public OrderApproval build() {
			return new OrderApproval(this);
		}
	}

}
